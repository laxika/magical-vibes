package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostByManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "73")
public class StormwingEntity extends Card {

    public StormwingEntity() {
        // This spell costs {2}{U} less to cast if you've cast an instant or sorcery spell this turn.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new ReduceOwnCastCostByManaCostEffect("{2}{U}")));

        // Prowess (Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn.)
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new BoostSelfEffect(1, 1))));

        // When this creature enters, scry 2.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
    }
}
