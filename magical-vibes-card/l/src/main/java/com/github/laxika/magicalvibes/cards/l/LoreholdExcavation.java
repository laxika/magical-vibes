package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "200")
public class LoreholdExcavation extends Card {

    public LoreholdExcavation() {
        // At the beginning of your end step, mill a card. If a land card was milled this way,
        // you gain 1 life. Otherwise, this enchantment deals 1 damage to each opponent.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MillControllerThenIfMilledEffect(
                1,
                new CardTypePredicate(CardType.LAND),
                new GainLifeEffect(1),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)));

        // {5}, Exile a creature card from your graveyard: Create a tapped 3/2 red and white Spirit
        // creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Spirit",
                                3,
                                2,
                                CardColor.RED,
                                Set.of(CardColor.RED, CardColor.WHITE),
                                List.of(CardSubtype.SPIRIT),
                                Set.of(),
                                Set.of(),
                                false,
                                true,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of())
                ),
                "{5}, Exile a creature card from your graveyard: Create a tapped 3/2 red and white Spirit creature token."
        ));
    }
}
