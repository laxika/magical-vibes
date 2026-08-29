package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "186")
public class WebstrikeElite extends Card {

    public WebstrikeElite() {
        PermanentPredicate artifactOrEnchantmentWithManaValueXOrLess = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentManaValueAtMostXPredicate()));

        addHandActivatedAbility(new ActivatedAbility(false, "{X}{G}{G}",
                List.of(new DestroyTargetPermanentEffect(artifactOrEnchantmentWithManaValueXOrLess),
                        new DrawCardEffect(1)),
                "Cycling {X}{G}{G} ({X}{G}{G}, Discard this card: Draw a card.)",
                new PermanentPredicateTargetFilter(artifactOrEnchantmentWithManaValueXOrLess,
                        "Target must be an artifact or enchantment with mana value X or less."),
                null, null, null, List.of(), 0, 1));
    }
}
