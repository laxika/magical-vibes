package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "165")
public class DeafeningClarion extends Card {

    public DeafeningClarion() {
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Deafening Clarion deals 3 damage to each creature",
                        DealDamageEffect.toEachCreature(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain lifelink until end of turn",
                        new GrantKeywordEffect(Keyword.LIFELINK, Subjects.otherCreaturesYouControl()))
        )));
    }
}
