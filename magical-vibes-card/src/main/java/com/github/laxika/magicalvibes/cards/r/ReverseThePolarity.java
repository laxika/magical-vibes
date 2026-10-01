package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchAllCreaturesPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "54")
@CardRegistration(set = "WHO", collectorNumber = "369")
@CardRegistration(set = "WHO", collectorNumber = "659")
@CardRegistration(set = "WHO", collectorNumber = "960")
public class ReverseThePolarity extends Card {

    public ReverseThePolarity() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Counter all other spells",
                        new CounterMatchingSpellsEffect(new StackEntryTruePredicate())),
                new ChooseOneEffect.ChooseOneOption("Switch each creature's power and toughness",
                        new SwitchAllCreaturesPowerToughnessEffect()),
                new ChooseOneEffect.ChooseOneOption("Creatures can't be blocked this turn",
                        new MakeAllCreaturesUnblockableEffect())
        )));
    }
}
