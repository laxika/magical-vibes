package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "18")
public class FirstLittlePig extends Card {

    public FirstLittlePig() {
        var artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G/W}",
                List.of(new ExileTargetPermanentEffect(artifactOrEnchantment)),
                "{1}{G/W}: Exile target artifact or enchantment. Activate only once.")
                .withMaxActivationsPerGame(1));
    }
}
