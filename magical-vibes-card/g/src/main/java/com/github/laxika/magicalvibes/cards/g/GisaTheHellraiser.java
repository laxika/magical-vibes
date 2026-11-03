package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "89")
public class GisaTheHellraiser extends Card {

    public GisaTheHellraiser() {
        // Ward—{2}, Pay 2 life.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2, 2));

        addStaticEffect(new StaticBoostEffect(1, 1, Set.of(Keyword.MENACE), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SKELETON, CardSubtype.ZOMBIE))));

        addTrigger(Triggers.youCommitCrime(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateTokenEffect(CardType.CREATURE, 2, "Zombie Rogue", 2, 2,
                        CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.BLACK),
                        List.of(CardSubtype.ZOMBIE, CardSubtype.ROGUE), Set.of(), Set.of(),
                        false, true, List.of(), List.of(), false, false, false, 0, Set.of()));
    }
}
