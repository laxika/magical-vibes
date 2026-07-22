package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChandraDressedToKillEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "148")
public class ChandraDressedToKill extends Card {

    public ChandraDressedToKill() {
        // +1: Add {R}. Chandra deals 1 damage to up to one target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new AwardManaEffect(ManaColor.RED, 1),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)
                ),
                "+1: Add {R}. Chandra, Dressed to Kill deals 1 damage to up to one target player or planeswalker.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a player or planeswalker"
                ),
                +1, null, null,
                List.of(), 0, 1
        ));

        // +1: Exile the top card of your library. If it's red, you may cast it this turn.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ExileTopCardsMayCastMatchingThisTurnEffect(
                        1, new CardColorPredicate(CardColor.RED))),
                "+1: Exile the top card of your library. If it's red, you may cast it this turn."
        ));

        // −7: Exile the top five cards of your library. You may cast red spells from among them this
        // turn. You get an emblem with "Whenever you cast a red spell, this emblem deals X damage to
        // any target, where X is the amount of mana spent to cast that spell."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new ExileTopCardsMayCastMatchingThisTurnEffect(
                                5, new CardColorPredicate(CardColor.RED)),
                        new ChandraDressedToKillEmblemEffect()
                ),
                "\u22127: Exile the top five cards of your library. You may cast red spells from among "
                        + "them this turn. You get an emblem with \"Whenever you cast a red spell, this "
                        + "emblem deals X damage to any target, where X is the amount of mana spent to "
                        + "cast that spell.\""
        ));
    }
}
