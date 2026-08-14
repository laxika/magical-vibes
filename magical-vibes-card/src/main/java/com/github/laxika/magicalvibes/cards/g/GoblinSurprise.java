package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "200")
public class GoblinSurprise extends Card {

    public GoblinSurprise() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+0 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 0)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 red Goblin creature tokens",
                        new CreateTokenEffect(2, "Goblin", 1, 1,
                                CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of())
                )
        )));
    }
}
