package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "80")
public class WharfInfiltrator extends Card {

    public WharfInfiltrator() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Draw a card and discard a card?"
        ));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.CREATURE),
                new MayPayManaEffect("{2}",
                        new CreateTokenEffect("Eldrazi Horror", 3, 2, null,
                                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), Set.of(), Set.of()),
                        "Pay {2} to create a 3/2 colorless Eldrazi Horror creature token?")
        ));
    }
}
