package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyCreatureDealingCombatDamageToPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "208")
public class VraskaTheUnseen extends Card {

    public VraskaTheUnseen() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RegisterDelayedDestroyCreatureDealingCombatDamageToPlaneswalkerEffect()),
                "+1: Until your next turn, whenever a creature deals combat damage to Vraska, destroy that creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "−3: Destroy target nonland permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent"
                )
        ));

        CreateTokenEffect assassin = new CreateTokenEffect(
                3, "Assassin", 1, 1,
                CardColor.BLACK, List.of(CardSubtype.ASSASSIN),
                Set.of(), Set.of(),
                Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerLosesGameEffect(null))
        );
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(assassin),
                "−7: Create three 1/1 black Assassin creature tokens with \"Whenever this creature deals combat damage to a player, that player loses the game.\""
        ));
    }
}
