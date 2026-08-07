package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

/**
 * The printed ability is modal ("Choose one —"), but a mode is chosen while the ability is being
 * activated (CR 601.2b via CR 602.2b), at the same time as its targets. Modelling each mode as its
 * own activated ability with the same cost is therefore behaviourally identical, and it lets every
 * mode declare the target restriction its own effect needs (the engine's modal ability wrapper
 * cannot carry per-mode targeting).
 */
@CardRegistration(set = "ORI", collectorNumber = "53")
public class DiscipleOfTheRing extends Card {

    private static final String COST = "{1}";

    public DiscipleOfTheRing() {
        // {1}, Exile an instant or sorcery card from your graveyard:
        // Counter target noncreature spell unless its controller pays {2}.
        addActivatedAbility(new ActivatedAbility(
                false,
                COST,
                List.of(exileInstantOrSorceryCost(), new CounterUnlessPaysEffect(2)),
                "{1}, Exile an instant or sorcery card from your graveyard: Counter target noncreature spell "
                        + "unless its controller pays {2}.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                        "Target must be a noncreature spell."
                )
        ));

        // {1}, Exile an instant or sorcery card from your graveyard: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                COST,
                List.of(exileInstantOrSorceryCost(), new BoostSelfEffect(1, 1)),
                "{1}, Exile an instant or sorcery card from your graveyard: This creature gets +1/+1 until end of turn."
        ));

        // {1}, Exile an instant or sorcery card from your graveyard: Tap target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                COST,
                List.of(exileInstantOrSorceryCost(),
                        new TapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "{1}, Exile an instant or sorcery card from your graveyard: Tap target creature."
        ));

        // {1}, Exile an instant or sorcery card from your graveyard: Untap target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                COST,
                List.of(exileInstantOrSorceryCost(),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "{1}, Exile an instant or sorcery card from your graveyard: Untap target creature."
        ));
    }

    private static ExileCardFromGraveyardCost exileInstantOrSorceryCost() {
        return new ExileCardFromGraveyardCost(CardType.INSTANT, CardType.SORCERY);
    }
}
