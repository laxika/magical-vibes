package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PlayersWithCardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "52")
public class WolfcallersHowl extends Card {

    public WolfcallersHowl() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                new PlayersWithCardsInHandAtLeast(CountScope.OPPONENTS, 4),
                "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()));
    }
}
