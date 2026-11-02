package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PlayersWithCardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "52")
@CardRegistration(set = "CMA", collectorNumber = "169")
public class WolfcallersHowl extends Card {

    public WolfcallersHowl() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenEffect(
                new PlayersWithCardsInHandAtLeast(CountScope.OPPONENTS, 4),
                "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()));
    }
}
