package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.v.VhalEagerScholar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveVhalStudyCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RemoveVhalStudyCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveVhalStudyCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (RemoveVhalStudyCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int removedStudyCounters = source.getCounterCount(CounterType.STUDY);
        source.setCounterCount(CounterType.STUDY, 0);
        enqueueSpecializedTrigger(gameData, entry, source.getCard(), removal.color(), removedStudyCounters);
    }

    private void enqueueSpecializedTrigger(GameData gameData, StackEntry entry, Card specialized,
                                           CardColor color, int removedStudyCounters) {
        // The face's target group is its specialize ability's, bound to that ability's effect as the face was made.
        CardEffect specializedTrigger = specialized.getTargetGroups().isEmpty()
                ? VhalEagerScholar.specializedTrigger(color)
                : specialized.getEffectsBoundToTargetGroup(0).getFirst();
        // The "when you do" ability (CR 603.12) is put on the stack by the stack door, which chooses its targets as it
        // goes there (CR 603.3d). The study counters removed are its event value (and, for the black face, its X).
        StackEntry trigger = TriggeredAbilityStackService.madeAbility(specialized, entry.getControllerId(),
                specialized.getName() + "'s ability", List.of(specializedTrigger),
                color == CardColor.BLACK ? removedStudyCounters : 0, entry.getSourcePermanentId());
        trigger.setEventValue(removedStudyCounters);
        triggeredAbilityStackService.putMade(gameData, trigger);
    }
}
