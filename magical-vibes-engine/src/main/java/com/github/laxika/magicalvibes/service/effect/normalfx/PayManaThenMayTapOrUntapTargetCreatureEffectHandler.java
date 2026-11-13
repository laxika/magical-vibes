package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaThenMayTapOrUntapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PayManaThenMayTapOrUntapTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayManaThenMayTapOrUntapTargetCreatureEffect.class;
    }

    /** The reflexive ability the payment makes: "you may tap or untap target creature". */
    private static final MayEffect MAY_TAP_OR_UNTAP = new MayEffect(
            new TapOrUntapTargetPermanentEffect(new PermanentIsCreaturePredicate()), "Tap or untap target creature?");

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var payThenTap = (PayManaThenMayTapOrUntapTargetCreatureEffect) effect;

        if (gameData.resolvedMayAccepted != null) {
            boolean paid = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (paid) {
                queueReflexiveTrigger(gameData, entry);
            }
            return;
        }

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(),
                entry.getCard().getName() + " - Pay " + payThenTap.manaCost() + " to use its ability?",
                null,
                payThenTap.manaCost(),
                entry.getSourcePermanentId()));
    }

    private void queueReflexiveTrigger(GameData gameData, StackEntry entry) {
        // "When you do" is an ability of its own (CR 603.12): its target is chosen as it goes on the stack, and it
        // records what the resolving entry was about.
        triggeredAbilityStackService.putMade(gameData,
                ReflexiveAbilitySupport.record(gameData, entry, List.of(MAY_TAP_OR_UNTAP)));
    }
}
