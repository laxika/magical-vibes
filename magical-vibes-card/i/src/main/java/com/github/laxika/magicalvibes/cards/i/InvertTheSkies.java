package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "EVE", collectorNumber = "155")
public class InvertTheSkies extends Card {

    public InvertTheSkies() {
        // Creatures your opponents control lose flying until end of turn if {G} was spent.
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN),
                new RemoveKeywordEffect(Keyword.FLYING, Subjects.creaturesYourOpponentsControl())));

        // Creatures you control gain flying until end of turn if {U} was spent.
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLUE),
                new GrantKeywordEffect(Keyword.FLYING, Subjects.otherCreaturesYouControl())));
    }
}
