package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "5")
public class LeoninBattlemage extends Card {

    public LeoninBattlemage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{T}: Target creature gets +1/+1 until end of turn.",
                TargetFilters.creature()
        ));

        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(new UntapPermanentsEffect(TapUntapScope.SELF))),
                "Untap Leonin Battlemage?"));
    }
}
