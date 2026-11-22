package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "177")
public class WaxingMoon extends Card {

    public WaxingMoon() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF),
                "Target must be a Werewolf you control"
        ), 0, 1).addSpellEffect(new TransformSelfEffect(Subjects.target()));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl()));
    }
}
