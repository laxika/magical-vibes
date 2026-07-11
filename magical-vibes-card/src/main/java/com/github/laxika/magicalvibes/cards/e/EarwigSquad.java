package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardsToExileEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "60")
public class EarwigSquad extends Card {

    public EarwigSquad() {
        // Prowl {2}{B}: cast for this cost if you dealt combat damage to a player this turn
        // with a Goblin or Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{B}")),
                Set.of(CardSubtype.GOBLIN, CardSubtype.ROGUE)));

        // When this creature enters, if its prowl cost was paid, search target opponent's
        // library for three cards and exile them. Then that player shuffles.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new CastForProwlCost(),
                        new SearchTargetLibraryForCardsToExileEffect(3)));
    }
}
