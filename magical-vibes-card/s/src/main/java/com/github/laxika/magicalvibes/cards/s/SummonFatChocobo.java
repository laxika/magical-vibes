package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "202")
@CardRegistration(set = "FIN", collectorNumber = "371")
public class SummonFatChocobo extends Card {

    public SummonFatChocobo() {
        CreateTokenEffect bird = new CreateTokenEffect(
                1, "Bird", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.BIRD), Set.of(), Set.of(),
                List.of(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new BoostSelfEffect(1, 0))));
        addTrigger(Triggers.chapter(1), bird);

        addTrigger(Triggers.chapter(2),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.chapter(3),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.chapter(4),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
    }
}
