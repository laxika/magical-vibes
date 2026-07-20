package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "136")
public class HazoretTheFervent extends Card {

    public HazoretTheFervent() {
        // Indestructible, haste — auto-loaded from Scryfall.

        // Hazoret the Fervent can't attack or block unless you have one or fewer cards in hand.
        // "one or fewer cards" = not (two or more cards).
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new NotCondition(new CardsInHandAtLeast(2)),
                "you have one or fewer cards in hand"
        ));

        // {2}{R}, Discard a card: Hazoret deals 2 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)
                ),
                "{2}{R}, Discard a card: Hazoret the Fervent deals 2 damage to each opponent."
        ));
    }
}
