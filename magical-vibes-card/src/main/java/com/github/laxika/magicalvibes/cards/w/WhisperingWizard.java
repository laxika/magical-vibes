package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "88")
public class WhisperingWizard extends Card {

    public WhisperingWizard() {
        // Whenever you cast a noncreature spell, create a 1/1 white Spirit creature token with flying.
        // This ability triggers only once each turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(CreateTokenEffect.whiteSpirit(1))
                )));
    }
}
