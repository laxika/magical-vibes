package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingArtifactUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "213")
@CardRegistration(set = "CMM", collectorNumber = "531")
public class DarettiScrapSavant extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever an artifact is put into your graveyard from the battlefield, return that card "
                    + "to the battlefield at the beginning of the next end step.";

    public DarettiScrapSavant() {
        // +2: Discard up to two cards, then draw that many cards.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DiscardUpToThenDrawThatManyEffect(2)),
                "+2: Discard up to two cards, then draw that many cards."
        ));

        // −2: Sacrifice an artifact. If you do, return target artifact card from your graveyard to the battlefield.
        ReturnCardFromGraveyardEffect returnArtifact = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .build();
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SacrificePermanentThenEffect(
                        new PermanentIsArtifactPredicate(), returnArtifact, "an artifact", true, false)),
                "−2: Sacrifice an artifact. If you do, return target artifact card from your graveyard "
                        + "to the battlefield.",
                new GraveyardCardPredicateTargetFilter(
                        new CardTypePredicate(CardType.ARTIFACT), GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
        ));

        // −10: You get an emblem with "Whenever an artifact is put into your graveyard from the battlefield,
        // return that card to the battlefield at the beginning of the next end step."
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new CreateEmblemEffect(
                        List.of(new RegisterDelayedReturnDyingArtifactUnderControlEffect()), EMBLEM_TEXT)),
                "−10: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
