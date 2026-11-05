package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;
import com.github.laxika.magicalvibes.model.Zone;

@CardRegistration(set = "WAR", collectorNumber = "92")
@CardRegistration(set = "EOC", collectorNumber = "83")
@CardRegistration(set = "FDC", collectorNumber = "101")
public class GodEternalBontu extends Card {

    public GodEternalBontu() {
        PermanentPredicate otherPermanent = new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(otherPermanent));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Bontu into its owner's library third from the top?"));
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS), new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Bontu into its owner's library third from the top?"));
    }
}
