package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "119")
public class ShamblingGhast extends Card {

    public ShamblingGhast() {
        addEffect(EffectSlot.ON_DEATH, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature an opponent controls gets -1/-1 until end of turn.",
                        new BoostTargetCreatureEffect(-1, -1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))),
                        TargetFilters.creatureAnOpponentControls()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Treasure token.",
                        CreateTokenEffect.ofTreasureToken(1))
        ))));
    }
}
