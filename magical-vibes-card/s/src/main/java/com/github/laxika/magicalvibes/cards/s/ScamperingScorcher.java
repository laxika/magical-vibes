package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "158")
public class ScamperingScorcher extends Card {

    public ScamperingScorcher() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(2, "Elemental", 1, 1, CardColor.RED,
                        List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL)));
    }
}
