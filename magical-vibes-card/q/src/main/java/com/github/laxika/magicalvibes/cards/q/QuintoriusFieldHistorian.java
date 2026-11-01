package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "220")
@CardRegistration(set = "SOC", collectorNumber = "326")
public class QuintoriusFieldHistorian extends Card {

    public QuintoriusFieldHistorian() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));

        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, new CreateTokenEffect(
                "Spirit", 3, 2, CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                List.of(CardSubtype.SPIRIT)));
    }
}
