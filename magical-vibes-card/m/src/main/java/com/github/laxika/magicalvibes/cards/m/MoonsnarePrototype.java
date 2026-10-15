package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "69")
public class MoonsnarePrototype extends Card {

    public MoonsnarePrototype() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )), true),
                        new AwardManaEffect(ManaColor.COLORLESS)
                ),
                "{T}, Tap an untapped artifact or creature you control: Add {C}."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0)),
                "Channel — {4}{U}, Discard this card: The owner of target nonland permanent puts it on their choice of the top or bottom of their library.",
                TargetFilters.nonlandPermanent()
        ));
    }
}
