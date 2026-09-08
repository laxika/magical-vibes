package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "227")
public class SpeedballNewWarrior extends Card {

    public SpeedballNewWarrior() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(
                                new BoostSelfEffect(2, 2),
                                new MayEffect(new ChooseNewTargetsForTargetSpellEffect(),
                                        "Choose new targets for the spell?")
                        ),
                        new StackEntryTargetsSourcePredicate()));
    }
}
