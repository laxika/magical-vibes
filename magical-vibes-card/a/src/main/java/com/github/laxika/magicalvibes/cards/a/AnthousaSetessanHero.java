package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "149")
public class AnthousaSetessanHero extends Card {

    public AnthousaSetessanHero() {
        // Heroic — Whenever you cast a spell that targets Anthousa, up to three target lands you
        // control each become 2/2 Warrior creatures until end of turn. They're still lands.
        target(TargetFilters.landYouControl(), 0, 3).addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new AnimatePermanentsEffect(
                        2, 2, List.of(CardSubtype.WARRIOR), Set.of(), null, Set.of(),
                        GrantScope.TARGET, Duration.UNTIL_END_OF_TURN));
    }
}
