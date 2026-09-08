package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForImprintedCardOwnerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenForImprintedCardOwnerEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenForImprintedCardOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card imprintedCard = gameData.getImprintedCard(entry.getCard());
        if (imprintedCard == null) {
            return;
        }

        ExiledCardEntry exileEntry = gameData.findExiledCard(imprintedCard.getId());
        if (exileEntry == null) {
            return;
        }

        UUID ownerId = exileEntry.ownerId();
        if (ownerId == null || !gameData.playerIds.contains(ownerId)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        var token = ((CreateTokenForImprintedCardOwnerEffect) effect).tokenEffect();
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, token.amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, token.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, token.toughness(), context);

        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, ownerId, token, amount, entry.getCard().getSetCode(), power, toughness));
    }
}
