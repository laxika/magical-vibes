package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNinjutsuAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "NEO", collectorNumber = "234")
public class SatoruUmezawa extends Card {

    public SatoruUmezawa() {
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY,
                new TriggeringNinjutsuAbilityConditionalEffect(
                        new OncePerTurnTriggerEffect(
                                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)))));
        addEffect(EffectSlot.STATIC, new GrantHandActivatedAbilityToCardsEffect(
                Card.ninjutsuAbility("{2}{U}{B}"), new CardTypePredicate(CardType.CREATURE), true));
    }
}
