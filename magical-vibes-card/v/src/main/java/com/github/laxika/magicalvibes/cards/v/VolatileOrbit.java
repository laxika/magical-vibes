package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "27")
public class VolatileOrbit extends Card {

    public VolatileOrbit() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLANET)),
                new DealDamageToAnyTargetEffect(4)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        DraftFromSpellbookEffect.conjureOntoBattlefieldWithCounters(List.of(
                                AdagiaWindsweptBastion::new,
                                EvendoWakingHaven::new,
                                KavaronMemorialWorld::new,
                                SusurSecundiVoidAltar::new,
                                UthrosTitanicGodcore::new), CounterType.CHARGE, 8)),
                "{1}{R}{G}, Sacrifice this enchantment: Conjure a card of your choice from the Planets Spellbook onto the battlefield tapped with eight charge counters on it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
