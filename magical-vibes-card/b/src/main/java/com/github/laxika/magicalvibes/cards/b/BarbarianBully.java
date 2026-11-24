package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "79")
public class BarbarianBully extends Card {

    public BarbarianBully() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardRandomCardCost(),
                        MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                                new DealDamageToPlayersEffect(4, Subjects.acceptedPlayers()),
                                "Have it deal 4 damage to you? If no player does, it gets +2/+2 until end of turn.",
                                null, new BoostSelfEffect(2, 2))),
                "Discard a card at random: This creature gets +2/+2 until end of turn unless a player has this creature deal 4 damage to them. Activate only once each turn.",
                1));
    }
}
