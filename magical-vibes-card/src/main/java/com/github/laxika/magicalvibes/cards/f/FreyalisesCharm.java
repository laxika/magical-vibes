package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "240")
public class FreyalisesCharm extends Card {

    public FreyalisesCharm() {
        // Whenever an opponent casts a black spell, you may pay {G}{G}. If you do, you draw a card.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLACK),
                        List.of(new MayPayManaEffect("{G}{G}", new DrawCardEffect(),
                                "Pay {G}{G} to draw a card?"))));

        // {G}{G}: Return this enchantment to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{G}{G}", List.of(ReturnToHandEffect.self()),
                "{G}{G}: Return Freyalise's Charm to its owner's hand."));
    }
}
