package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "43")
@CardRegistration(set = "BRO", collectorNumber = "102")
@CardRegistration(set = "SLD", collectorNumber = "1986")
@CardRegistration(set = "2X2", collectorNumber = "76")
@CardRegistration(set = "ACR", collectorNumber = "91")
@CardRegistration(set = "SLZ", collectorNumber = "42")
@CardRegistration(set = "SLZ", collectorNumber = "163")
@CardRegistration(set = "SLZ", collectorNumber = "284")
@CardRegistration(set = "MOC", collectorNumber = "250")
@CardRegistration(set = "40K", collectorNumber = "201")
@CardRegistration(set = "LTC", collectorNumber = "201")
@CardRegistration(set = "MIC", collectorNumber = "119")
@CardRegistration(set = "C17", collectorNumber = "114")
public class GoForTheThroat extends Card {

    public GoForTheThroat() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate())
                )),
                "Target must be a nonartifact creature"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
