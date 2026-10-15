package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasMorphAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "43")
public class MasterOfTheVeil extends Card {

    public MasterOfTheVeil() {
        addMorph("{2}{U}");
        PermanentAllOfPredicate creatureWithMorph = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasMorphAbilityPredicate()));
        target(new PermanentPredicateTargetFilter(
                creatureWithMorph,
                "Target must be a creature with a morph ability"
        )).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new MayEffect(
                        new TurnTargetCreatureFaceDownEffect(new PermanentHasMorphAbilityPredicate()),
                        "Turn target creature with a morph ability face down?"));
    }
}
