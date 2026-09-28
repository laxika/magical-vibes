package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfAttackingModifiedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Mirror-Style Master's attack trigger. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfAttackingModifiedCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private static final PermanentIsModifiedPredicate MODIFIED = new PermanentIsModifiedPredicate();
    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_COPY_PROFILE =
            CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingExiledAtEndOfCombat();

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfAttackingModifiedCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect rawEffect) {
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(controllerId);
        List<Permanent> attackers = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(Permanent::isAttacking)
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, MODIFIED, filterContext))
                .toList();

        for (Permanent attacker : attackers) {
            tokenCopySupport.createTokenCopies(
                    gameData, entry, List.of(attacker.getCard()), attacker, controllerId, TOKEN_COPY_PROFILE);
        }
    }
}
