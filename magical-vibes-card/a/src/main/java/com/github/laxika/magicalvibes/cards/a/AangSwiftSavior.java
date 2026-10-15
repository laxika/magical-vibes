package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetCreatureOrSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "204")
public class AangSwiftSavior extends Card {

    public AangSwiftSavior() {
        setBackFaceCard(new AangAndLaOceansFury());

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(anotherCreature,
                "Target must be another creature"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AirbendTargetCreatureOrSpellEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new WaterbendCost(8), new TransformSelfEffect()),
                "Waterbend {8}: Transform Aang."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "AangAndLaOceansFury";
    }
}
