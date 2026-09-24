package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "854")
public class TombTyrant extends Card {

    public TombTyrant() {
        // Other Zombies you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));

        // {2}{B}, {T}, Sacrifice a creature: Return a Zombie creature card at random from your
        // graveyard to the battlefield. Activate only during your turn and only if there are at
        // least three Zombie creature cards in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new SacrificeCreatureCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.ZOMBIE)
                                )))
                                .returnAtRandom(true)
                                .build()
                ),
                "{2}{B}, {T}, Sacrifice a creature: Return a Zombie creature card at random from "
                        + "your graveyard to the battlefield. Activate only during your turn and "
                        + "only if there are at least three Zombie creature cards in your graveyard.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withRequiredGraveyardCards(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.ZOMBIE)
                )),
                3,
                "Zombie creature cards in your graveyard"));
    }
}
