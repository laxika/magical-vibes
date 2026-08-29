package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "80")
public class GoblinWelder extends Card {

    public GoblinWelder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect()),
                "{T}: Choose target artifact a player controls and target artifact card in that player's graveyard. "
                        + "If both targets are still legal as this ability resolves, that player simultaneously "
                        + "sacrifices the artifact and returns the artifact card to the battlefield.",
                List.of(
                        TargetFilters.artifact(),
                        new GraveyardCardPredicateTargetFilter(
                                new CardTypePredicate(CardType.ARTIFACT), GraveyardSearchScope.ALL_GRAVEYARDS)),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));
    }
}
