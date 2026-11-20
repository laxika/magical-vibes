package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "156")
@CardRegistration(set = "TSR", collectorNumber = "250")
public class DormantSliver extends Card {

    public DormantSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        addStaticEffect(new GrantKeywordEffect(
                Keyword.DEFENDER,
                Subjects.otherPermanents(),
                sliver));
        addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, Subjects.source(), sliver));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(1)),
                Subjects.otherPermanents(),
                sliver));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                        new DrawCardEffect(1)), Subjects.source(), sliver));
    }
}
