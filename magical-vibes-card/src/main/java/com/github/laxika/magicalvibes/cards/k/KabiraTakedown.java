package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "19")
public class KabiraTakedown extends Card {

    public KabiraTakedown() {
        setBackFaceCard(new KabiraPlateau());
        setModalDoubleFaced(true);

        PermanentPredicateTargetFilter creatureOrPlaneswalker = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be a creature or planeswalker");
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Kabira Takedown deals damage equal to the number of creatures you control to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new PermanentCount(
                                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)),
                        creatureOrPlaneswalker).withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption("Kabira Plateau", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "KabiraPlateau";
    }
}
