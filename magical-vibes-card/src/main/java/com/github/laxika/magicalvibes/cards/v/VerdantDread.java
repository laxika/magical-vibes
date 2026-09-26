package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "17")
public class VerdantDread extends Card {

    public VerdantDread() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ManifestDreadEffect.forController());
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNamedPredicate("Verdant Dread"),
                        ManifestDreadEffect.forController()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(new ConjureCardNamedOntoBattlefieldEffect("YDSK", "17")),
                "{3}{G}{G}: Conjure a card named Verdant Dread onto the battlefield. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
