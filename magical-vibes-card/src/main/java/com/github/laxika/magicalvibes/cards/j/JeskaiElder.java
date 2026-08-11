package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "43")
public class JeskaiElder extends Card {

    public JeskaiElder() {
        // Prowess (Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn.)
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new BoostSelfEffect(1, 1))
        ));

        // Whenever this creature deals combat damage to a player, you may draw a card. If you do,
        // discard a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "Draw a card, then discard a card?"
        ));
    }
}
