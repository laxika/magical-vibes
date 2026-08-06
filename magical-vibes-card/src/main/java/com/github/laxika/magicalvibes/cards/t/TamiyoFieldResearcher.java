package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "249")
public class TamiyoFieldResearcher extends Card {

    public TamiyoFieldResearcher() {
        // +1: Choose up to two target creatures. Until your next turn, whenever either of those
        // creatures deals combat damage, you draw a card. The watch is registered as a delayed
        // trigger controlled by Tamiyo's controller, so "you draw" draws for them even when the
        // chosen creature belongs to an opponent.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new RegisterDelayedWatchedCreaturesCombatDamageEffect(List.of(new DrawCardEffect(1)))),
                "+1: Choose up to two target creatures. Until your next turn, whenever either of those "
                        + "creatures deals combat damage, you draw a card.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature(), TargetFilters.creature()), 0, 2));

        // −2: Tap up to two target nonland permanents. They don't untap during their controller's
        // next untap step.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET), new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "−2: Tap up to two target nonland permanents. They don't untap during their controller's "
                        + "next untap step.",
                null, -2, null, null,
                List.<TargetFilter>of(TargetFilters.nonlandPermanent(), TargetFilters.nonlandPermanent()), 0, 2));

        // −7: Draw three cards. You get an emblem with "You may cast spells from your hand without
        // paying their mana costs." The emblem carries Omniscience's hand-only {0} alternative cost.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new DrawCardEffect(3),
                        new CreateEmblemEffect(
                                List.of(new AlternativeCostForSpellsEffect("{0}", null, null, false, true)),
                                "You may cast spells from your hand without paying their mana costs.")),
                "−7: Draw three cards. You get an emblem with \"You may cast spells from your hand "
                        + "without paying their mana costs.\""
        ));
    }
}
