package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves Redoubled Stormsinger's temporary token-copy attack trigger. The copies of every eligible token are one
 * token creation event whose replacements the controller orders; the attack target of each creature token it
 * creates is chosen afterwards, as the tokens enter.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachTokenEnteredThisTurnEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentEnteredBattlefieldThisTurnPredicate ENTERED_THIS_TURN =
            new PermanentEnteredBattlefieldThisTurnPredicate();
    private static final CreateTokenCopyOfTargetPermanentEffect TAPPED_ATTACKING_SACRIFICED_COPY =
            new CreateTokenCopyOfTargetPermanentEffect(false, false, true, true);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachTokenEnteredThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> matchingTokens = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, ENTERED_THIS_TURN))
                .map(Permanent::getCard)
                .toList();
        if (matchingTokens.isEmpty()) {
            return;
        }
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopiesChoosingAttackTargets(gameData, entry, matchingTokens, source,
                controllerId, TAPPED_ATTACKING_SACRIFICED_COPY);
    }
}
