package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "211")
public class RalStormConduit extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)
    ));

    public RalStormConduit() {
        List<CardEffect> damageAbility = List.of(
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1, PlayerRelation.OPPONENT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(INSTANT_OR_SORCERY, damageAbility));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(INSTANT_OR_SORCERY, damageAbility));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new ScryEffect(1)),
                "+2: Scry 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CopyNextInstantOrSorceryCastThisTurnEffect()),
                "−2: When you next cast an instant or sorcery spell this turn, copy that spell. "
                        + "You may choose new targets for the copy."
        ));
    }
}
