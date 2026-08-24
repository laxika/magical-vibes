package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MarchesaResoluteMonarch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "114")
public class InvasionOfFiora extends Card {

    public InvasionOfFiora() {
        setBackFaceCard(new MarchesaResoluteMonarch());

        PermanentAllOfPredicate legendaryCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        PermanentAllOfPredicate nonlegendaryCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all legendary creatures",
                        new DestroyAllPermanentsEffect(legendaryCreatures)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all nonlegendary creatures",
                        new DestroyAllPermanentsEffect(nonlegendaryCreatures)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "MarchesaResoluteMonarch";
    }
}
