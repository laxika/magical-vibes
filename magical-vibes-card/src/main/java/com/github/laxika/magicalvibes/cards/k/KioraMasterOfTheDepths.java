package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayFightTargetCreatureOnAllyCreatureEntersEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "213")
public class KioraMasterOfTheDepths extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever a creature you control enters, you may have it fight target creature.";

    public KioraMasterOfTheDepths() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "+1: Untap up to one target creature and up to one target land.",
                null, +1, null, null,
                List.of(TargetFilters.creature(), TargetFilters.land()), 0, 2
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                        4, CardType.CREATURE, CardType.LAND, List.of(),
                        LookDestination.GRAVEYARD, true)),
                "-2: Reveal the top four cards of your library. You may put a creature card and/or a land "
                        + "card from among them into your hand. Put the rest into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new CreateEmblemEffect(
                                List.of(new MayFightTargetCreatureOnAllyCreatureEntersEffect.Marker()),
                                EMBLEM_TEXT),
                        new CreateTokenEffect(
                                3, "Octopus", 8, 8, CardColor.BLUE,
                                List.of(CardSubtype.OCTOPUS), Set.of(), Set.of())
                ),
                "-8: You get an emblem with \"" + EMBLEM_TEXT + "\". Then create three 8/8 blue Octopus "
                        + "creature tokens."
        ));
    }
}
