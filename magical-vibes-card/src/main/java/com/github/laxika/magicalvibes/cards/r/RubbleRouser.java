package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "128")
public class RubbleRouser extends Card {

    public RubbleRouser() {
        // When this creature enters, you may discard a card. If you do, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));

        // {T}, Exile a card from your graveyard: Add {R}. When you do, this creature deals
        // 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileCardFromGraveyardCost(null),
                        new AwardManaEffect(ManaColor.RED, 1),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)
                ),
                "{T}, Exile a card from your graveyard: Add {R}. When you do, this creature deals 1 damage to each opponent."
        ));
    }
}
