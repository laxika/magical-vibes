package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNinjutsuAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "234")
@CardRegistration(set = "SLD", collectorNumber = "2283")
public class SatoruUmezawa extends Card {

    public SatoruUmezawa() {
        addTrigger(Triggers.youActivateAbility(),
                new TriggeringNinjutsuAbilityConditionalEffect(
                        new OncePerTurnTriggerEffect(
                                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)))));
        addStaticEffect(new GrantHandActivatedAbilityToCardsEffect(
                Card.ninjutsuAbility("{2}{U}{B}"), new CardTypePredicate(CardType.CREATURE), true));
    }
}
