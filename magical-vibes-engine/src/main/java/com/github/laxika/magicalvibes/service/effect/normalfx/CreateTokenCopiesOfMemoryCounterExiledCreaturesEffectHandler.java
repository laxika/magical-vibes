package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfMemoryCounterExiledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves Altaïr Ibn-La'Ahad's "for each creature card you own in exile with a memory counter on it, create a
 * tapped and attacking token that's a copy of it. Exile those tokens at end of combat." The copies are one ordered
 * token creation event (CR 616.1), and the effect doesn't say what the tokens attack, so their controller chooses
 * for each creature token, doubled copies and added tokens included (CR 508.4).
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfMemoryCounterExiledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_COPY_PROFILE =
            CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingExiledAtEndOfCombat();

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfMemoryCounterExiledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> sourceCards = gameData.getPlayerExiledCards(entry.getControllerId()).stream()
                .filter(card -> gameData.exiledCardsWithMemoryCounters.contains(card.getId()))
                .filter(card -> !card.isToken() && card.hasType(CardType.CREATURE))
                .toList();
        if (sourceCards.isEmpty()) {
            return;
        }
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopiesChoosingAttackTargets(
                gameData, entry, sourceCards, source, entry.getControllerId(), TOKEN_COPY_PROFILE);
    }
}
