package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;

@CardRegistration(set = "IKO", collectorNumber = "183")
public class DireTactics extends Card {

    public DireTactics() {
        target(TargetFilters.creature()).addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(
                        new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))),
                        new LoseLifeEffect(new TargetToughness(), Subjects.you())));
    }
}
