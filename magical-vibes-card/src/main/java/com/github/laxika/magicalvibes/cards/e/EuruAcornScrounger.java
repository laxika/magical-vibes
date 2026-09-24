package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

@CardRegistration(set = "YBLB", collectorNumber = "24")
public class EuruAcornScrounger extends Card {

    public EuruAcornScrounger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ForageEffect(new ConjureCardToBattlefieldEffect("MH2", "153")),
                "Forage?"));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL),
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentIsTokenPredicate(),
                                        new PutCounterOnEachControlledPermanentEffect(
                                                CounterType.ACORN, 1,
                                                new PermanentNamedPredicate("Chitterspitter")),
                                        "a token"),
                                "Sacrifice a token?"),
                        false,
                        true));
    }
}
