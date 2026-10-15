package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "1")
public class HashatonScarabsFist extends Card {

    public HashatonScarabsFist() {
        CreateTokenCopyOfTargetPermanentEffect zombieCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.ZOMBIE),
                Set.of(),
                4,
                4,
                Map.of(),
                false,
                false,
                false,
                false,
                false,
                false,
                CardColor.BLACK,
                Set.of(),
                false,
                Map.of(),
                List.of(),
                true,
                false,
                new Fixed(1),
                false,
                Set.of(),
                false);

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new MayPayManaEffect(
                                "{2}{U}",
                                CreateTokenCopyOfCardEffect.fromTriggeringCard(zombieCopy),
                                "Pay {2}{U} to create a tapped 4/4 black Zombie?")));
    }
}
