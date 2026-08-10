package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "126")
public class OneDozenEyes extends Card {

    public OneDozenEyes() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{G}{G}{G}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 5/5 green Beast creature token",
                        new CreateTokenEffect("Beast", 5, 5, CardColor.GREEN, List.of(CardSubtype.BEAST),
                                Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Create five 1/1 green Insect creature tokens",
                        new CreateTokenEffect(5, "Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.INSECT), Set.of(), Set.of()))
        )));
    }
}
