package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyMerfolkTappedForSourceAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "32")
public class VodalianWarMachine extends Card {

    public VodalianWarMachine() {
        PermanentHasSubtypePredicate merfolk = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(merfolk, false, false, true),
                        new CanAttackAsThoughNoDefenderEffect()),
                "Tap an untapped Merfolk you control: This creature can attack this turn as though it didn't have defender."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(merfolk, false, false, true),
                        new BoostSelfEffect(2, 1)),
                "Tap an untapped Merfolk you control: This creature gets +2/+1 until end of turn."
        ));
        addEffect(EffectSlot.ON_DEATH, new DestroyMerfolkTappedForSourceAbilitiesEffect());
    }
}
