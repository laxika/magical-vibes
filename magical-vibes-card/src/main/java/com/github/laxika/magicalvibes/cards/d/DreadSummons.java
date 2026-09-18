package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndCreateTokensForMilledCreaturesEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "20")
public class DreadSummons extends Card {

    public DreadSummons() {
        CreateTokenEffect tappedZombie = new CreateTokenEffect(
                1, "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true);
        addEffect(EffectSlot.SPELL,
                new MillEachPlayerAndCreateTokensForMilledCreaturesEffect(new XValue(), tappedZombie));
    }
}
