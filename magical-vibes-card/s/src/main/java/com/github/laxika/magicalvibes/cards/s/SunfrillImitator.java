package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "62")
@CardRegistration(set = "LCC", collectorNumber = "94")
public class SunfrillImitator extends Card {

    public SunfrillImitator() {
        PermanentPredicate anotherDinosaurYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        BecomeCopyOfTargetCreaturePermanentlyEffect copyEffect =
                new BecomeCopyOfTargetCreaturePermanentlyEffect(
                        "Sunfrill Imitator", EffectSlot.ON_ATTACK, anotherDinosaurYouControl);
        target(new PermanentPredicateTargetFilter(
                anotherDinosaurYouControl,
                "Target must be another Dinosaur you control"
        )).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                copyEffect,
                "Have Sunfrill Imitator become a copy of the target Dinosaur?"));
    }
}
