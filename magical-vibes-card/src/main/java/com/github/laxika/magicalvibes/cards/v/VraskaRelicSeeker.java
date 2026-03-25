package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPlayerLifeToSpecificValueEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "232")
public class VraskaRelicSeeker extends Card {

    public VraskaRelicSeeker() {
        // +2: Create a 2/2 black Pirate creature token with menace.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new CreateTokenEffect("Pirate", 2, 2,
                        CardColor.BLACK, List.of(CardSubtype.PIRATE),
                        Set.of(Keyword.MENACE), Set.of())),
                "+2: Create a 2/2 black Pirate creature token with menace."
        ));

        // −3: Destroy target artifact, creature, or enchantment. Create a Treasure token.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect(false), CreateTokenEffect.ofTreasureToken(1)),
                "\u22123: Destroy target artifact, creature, or enchantment. Create a Treasure token.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact, creature, or enchantment"
                )
        ));

        // −10: Target player's life total becomes 1.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new SetTargetPlayerLifeToSpecificValueEffect(1)),
                "\u221210: Target player's life total becomes 1.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Must target a player"
                )
        ));
    }
}
