package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeRestOfGameEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "11")
public class WelcomeTheDarkness extends Card {

    public WelcomeTheDarkness() {
        setMinimumXValue(1);
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Demon", new XValue(), new XValue(), CardColor.BLACK,
                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new SetLifeTotalEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new PlayersCantGainLifeRestOfGameEffect());
    }
}
