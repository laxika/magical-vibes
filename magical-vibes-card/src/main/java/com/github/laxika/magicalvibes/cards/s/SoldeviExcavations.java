package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAsEntersOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "143")
public class SoldeviExcavations extends Card {

    public SoldeviExcavations() {
        // If Soldevi Excavations would enter, sacrifice an untapped Island instead. If you do,
        // put this land onto the battlefield. If you don't, put it into its owner's graveyard.
        addEffect(EffectSlot.STATIC, new SacrificePermanentAsEntersOrGraveyardEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate())
                )),
                "an untapped Island"));

        // {T}: Add {C}{U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS), new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {C}{U}."
        ));

        // {1}, {T}: Scry 1.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ScryEffect(1)),
                "{1}, {T}: Scry 1."
        ));
    }
}
