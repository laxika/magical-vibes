package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "18")
public class BroodAstronomer extends Card {

    public BroodAstronomer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsLandPredicate(),
                        new DraftFromSpellbookEffect(List.of(
                                AdagiaWindsweptBastion::new,
                                EvendoWakingHaven::new,
                                KavaronMemorialWorld::new,
                                SusurSecundiVoidAltar::new,
                                UthrosTitanicGodcore::new)),
                        "a land"),
                "Sacrifice a land?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ConditionalAnyColorManaEffect(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.PLANET),
                                new PermanentCounterCountAtLeastPredicate(CounterType.CHARGE, 12)
                        ))), 3, 1)),
                "{T}: Add one mana of any color. If you control a Planet with twelve or more charge counters on it, add three mana of any one color instead."
        ));
    }
}
