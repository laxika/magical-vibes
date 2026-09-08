package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Creates a token and links the first token created to the source permanent. Additional tokens from
 * a token-doubling replacement effect are not linked, matching the singular token reference in the
 * card text.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenAndLinkToSourceEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAndLinkToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenAndLinkToSourceEffect) effect;
        Permanent liveSource = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent amountSource = liveSource != null ? liveSource : entry.getSourcePermanentSnapshot();
        AmountContext context = AmountContext.forStackEntry(entry, amountSource);
        int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), context);
        if (amount <= 0) {
            return;
        }

        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);
        List<UUID> created = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), amount, entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(created);

        if (liveSource == null || created.isEmpty()) {
            return;
        }

        Permanent token = gameQueryService.findPermanentById(gameData, created.getFirst());
        if (token == null) {
            return;
        }

        Card tokenCard = token.getCard().createRuntimeCopy();
        tokenCard.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE));
        tokenCard.freeze();
        token.setCard(tokenCard);

        liveSource.setChosenPermanentId(token.getId());
        token.setChosenPermanentId(liveSource.getId());
    }
}
