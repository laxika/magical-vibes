package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "128")
public class BondsOfMortality extends Card {

    public BondsOfMortality() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(
                new RemoveKeywordEffect(Keyword.HEXPROOF, GrantScope.OPPONENT_CREATURES),
                new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OPPONENT_CREATURES)),
                "{G}: Creatures your opponents control lose hexproof and indestructible until end of turn."));
    }
}
