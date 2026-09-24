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
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.function.Supplier;

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
                        new DraftFromSpellbookEffect(List.of(
                                chargedPlanet(AdagiaWindsweptBastion::new),
                                chargedPlanet(EvendoWakingHaven::new),
                                chargedPlanet(KavaronMemorialWorld::new),
                                chargedPlanet(SusurSecundiVoidAltar::new),
                                chargedPlanet(UthrosTitanicGodcore::new)), 5)),
                "{1}{R}{G}, Sacrifice this enchantment: Conjure a card of your choice from the Planets Spellbook onto the battlefield tapped with eight charge counters on it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    private static Supplier<? extends Card> chargedPlanet(Supplier<? extends Card> factory) {
        return () -> {
            Card card = factory.get();
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                    new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(8)));
            return card;
        };
    }
}
