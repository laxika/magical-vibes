package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.Imbraham;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongStudyCounterCardsInExile;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardToHandOrStudyCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "152")
public class Kianne extends Card {

    public Kianne() {
        Imbraham backFace = new Imbraham();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new ExileTopCardToHandOrStudyCounterEffect()),
                "{T}: Exile the top card of your library. If it's a land card, put it into your hand. "
                        + "Otherwise, put a study counter on it."));
        addActivatedAbility(new ActivatedAbility(false, "{4}{G}", List.of(
                new CreateTokenEffect(1, "Fractal", 0, 0, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL)),
                new PutCountersOnCreatedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new DistinctManaValuesAmongStudyCounterCardsInExile())),
                "{4}{G}: Create a 0/0 green and blue Fractal creature token. Put a +1/+1 counter on it "
                        + "for each different mana value among nonland cards you own in exile with study "
                        + "counters on them."));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Kianne, Dean of Substance", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Imbraham, Dean of Theory", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "Imbraham";
    }
}
