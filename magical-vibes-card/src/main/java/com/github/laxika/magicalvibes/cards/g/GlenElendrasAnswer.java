package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsSpellsAndAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "52")
public class GlenElendrasAnswer extends Card {

    public GlenElendrasAnswer() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new CounterOpponentsSpellsAndAbilitiesEffect(
                new CreateTokenEffect(1, "Faerie", 1, 1, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.BLACK), List.of(CardSubtype.FAERIE),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
