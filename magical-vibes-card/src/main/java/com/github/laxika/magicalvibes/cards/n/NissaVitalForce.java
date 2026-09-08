package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardOnAllyLandEntersEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "163")
public class NissaVitalForce extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever a land you control enters, you may draw a card.";

    public NissaVitalForce() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new AnimatePermanentsEffect(
                                5, 5, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), CardColor.GREEN,
                                Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_YOUR_NEXT_TURN)
                ),
                "+1: Untap target land you control. Until your next turn, it becomes a 5/5 green "
                        + "Elemental creature with haste. It's still a land.",
                TargetFilters.landYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsPermanentPredicate())
                        .targetGraveyard(true)
                        .build()),
                "−3: Return target permanent card from your graveyard to your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new DrawCardOnAllyLandEntersEffect.Marker()), EMBLEM_TEXT)),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
