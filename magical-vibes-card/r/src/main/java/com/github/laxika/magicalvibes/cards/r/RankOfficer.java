package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "102")
public class RankOfficer extends Card {

    public RankOfficer() {
        // When this creature enters, you may discard a card. If you do, create a 2/2 black Zombie
        // creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(null, CreateTokenEffect.blackZombie(1), "a card"),
                "Discard a card to create a 2/2 black Zombie creature token?"
        ));

        // {1}{B}, {T}, Exile a creature card from your graveyard: Each opponent loses 2 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{1}{B}, {T}, Exile a creature card from your graveyard: Each opponent loses 2 life."
        ));
    }
}
