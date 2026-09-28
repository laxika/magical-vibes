package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PlantTadpoles;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "122")
public class IllithidHarvester extends Card {

    public IllithidHarvester() {
        setBackFaceCard(new PlantTadpoles());
        addCastingOption(new AdventureCast("{X}{U}{U}"));

        PermanentAllOfPredicate tappedNontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                tappedNontokenCreature,
                "Targets must be tapped nontoken creatures"
        ), 0, 99).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TurnTargetCreatureFaceDownEffect(tappedNontokenCreature, Set.of(CardSubtype.HORROR)));
    }

    @Override
    public String getBackFaceClassName() {
        return "PlantTadpoles";
    }
}
