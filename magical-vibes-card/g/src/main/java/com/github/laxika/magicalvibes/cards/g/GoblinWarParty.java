package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "131")
public class GoblinWarParty extends Card {

    public GoblinWarParty() {
        addCastingCost(EscalateCost.entwine("{2}{R}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create three 1/1 red Goblin creature tokens",
                        new CreateTokenEffect(3, "Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 and gain haste until end of turn",
                        List.of(
                                BoostEffect.creaturesYouControl(1, 1),
                                new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl())))
        )));
    }
}
