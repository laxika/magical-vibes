package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes independent opponent choices before a simultaneous attacking-token-copy event. */
@Component
@RequiredArgsConstructor
public class CreateAttackingCopiesForOtherOpponentsHandler implements MayEffectHandlerBean {

    private final CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffectHandler copyHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect) ability.effects().getFirst();
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                ability.controllerId(), ability.description(), ability.effects(),
                ability.targetCardId(), ability.sourcePermanentId());
        entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        entry.setAttackedTargetId(ability.attackedTargetId());
        if (effect.opponentId() == null) {
            if (accepted) copyHandler.resolve(gameData, entry, effect);
        } else {
            copyHandler.completeChoice(gameData, entry, effect, accepted);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
