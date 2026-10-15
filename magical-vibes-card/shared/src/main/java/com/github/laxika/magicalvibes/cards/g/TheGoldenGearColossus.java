package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsDoubleFacedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

public class TheGoldenGearColossus extends Card {

    private static final CreateTokenEffect CREATE_GNOMES = new CreateTokenEffect(
            2, "Gnome", 1, 1, null, List.of(CardSubtype.GNOME), Set.of(), Set.of(CardType.ARTIFACT));

    private static final ControlledPermanentPredicateTargetFilter OTHER_DOUBLE_FACED_ARTIFACT =
            new ControlledPermanentPredicateTargetFilter(
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsArtifactPredicate(),
                            new PermanentIsDoubleFacedPredicate(),
                            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                    "Target must be another double-faced artifact you control");

    public TheGoldenGearColossus() {
        target(OTHER_DOUBLE_FACED_ARTIFACT, 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TransformTargetPermanentEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CREATE_GNOMES)
                .addEffect(EffectSlot.ON_ATTACK, new TransformTargetPermanentEffect())
                .addEffect(EffectSlot.ON_ATTACK, CREATE_GNOMES);
    }
}
