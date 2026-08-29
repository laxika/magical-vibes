package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "91")
public class TheSibsigCeremony extends Card {

    public TheSibsigCeremony() {
        // Creature spells you cast cost {2} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), new Fixed(2), CostModificationScope.SELF));

        // Whenever a creature you control enters, if you cast it, destroy that creature, then
        // create a 2/2 black Zombie Druid creature token.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new ConditionalEffect(
                new WasCast(), SequenceEffect.of(
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING),
                        new CreateTokenEffect("Zombie Druid", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE, CardSubtype.DRUID), Set.of(), Set.of()))));
    }
}
