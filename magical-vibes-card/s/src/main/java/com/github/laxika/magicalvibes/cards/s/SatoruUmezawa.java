package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "234")
@CardRegistration(set = "SLD", collectorNumber = "2283")
public class SatoruUmezawa extends Card {

    public SatoruUmezawa() {
        addTrigger(Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
        addStaticEffect(new GrantHandActivatedAbilityToCardsEffect(
                Card.ninjutsuAbility("{2}{U}{B}"), new CardTypePredicate(CardType.CREATURE), true));
    }
}
