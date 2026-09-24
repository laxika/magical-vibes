package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1422")
public class DarettiIngeniousIconoclast extends Card {

    public DarettiIngeniousIconoclast() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(
                        1, "Construct", 1, 1, null,
                        List.of(CardSubtype.CONSTRUCT), Set.of(Keyword.DEFENDER),
                        Set.of(CardType.ARTIFACT))),
                "+1: Create a 1/1 colorless Construct artifact creature token with defender."
        ));

        DestroyTargetPermanentEffect destroyArtifactOrCreature = new DestroyTargetPermanentEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())));
        SacrificePermanentThenEffect sacrificeThenDestroy = new SacrificePermanentThenEffect(
                new PermanentIsArtifactPredicate(), destroyArtifactOrCreature, "an artifact", true, false);
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new MayEffect(sacrificeThenDestroy, "Sacrifice an artifact?")),
                "−1: You may sacrifice an artifact. If you do, destroy target artifact or creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffect(3)),
                "−6: Choose target artifact card in a graveyard or artifact on the battlefield. Create three tokens that are copies of it."
        ));
    }
}
