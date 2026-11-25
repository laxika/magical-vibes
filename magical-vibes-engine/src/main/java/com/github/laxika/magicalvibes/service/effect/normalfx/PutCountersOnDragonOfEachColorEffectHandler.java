package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnDragonOfEachColorEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link PutCountersOnDragonOfEachColorEffect} one color at a time: it adds the Dragon the previous color's
 * counter step put a counter on ({@code PUT_COUNTERS}) to its tally, then inserts the next color's choice of a Dragon
 * you control of that color, the counter on {@code Subjects.thisWay(StepKind.CHOOSE)} and itself for the color after.
 * After the last color it inserts the win when five different Dragons got a counter.
 */
@Component
public class PutCountersOnDragonOfEachColorEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnDragonOfEachColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tally = (PutCountersOnDragonOfEachColorEffect) effect;
        Set<UUID> countered = new LinkedHashSet<>(tally.dragonsCountered());
        if (tally.nextColor() > 0) {
            countered.addAll(entry.getResolutionResults().outcome(StepKind.PUT_COUNTERS).objectIds());
        }
        CardColor[] colors = CardColor.values();
        List<CardEffect> next = new ArrayList<>();
        if (tally.nextColor() < colors.length) {
            CardColor color = colors[tally.nextColor()];
            next.add(ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                            new PermanentAllOfPredicate(List.of(
                                    new PermanentIsCreaturePredicate(),
                                    new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                                    new PermanentColorInPredicate(Set.of(color))))),
                    "a " + color.name().toLowerCase(Locale.ROOT) + " Dragon you control"));
            next.add(new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                    CounterType.PLUS_ONE_PLUS_ONE));
            next.add(new PutCountersOnDragonOfEachColorEffect(tally.nextColor() + 1, List.copyOf(countered)));
        } else if (countered.size() >= colors.length) {
            next.add(new WinGameEffect());
        }
        if (!next.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, next);
        }
    }
}
