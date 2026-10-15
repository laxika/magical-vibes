package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nissa, Sage Animist — back face of Nissa, Vastwood Seer.
 * Legendary Planeswalker — Nissa (Green).
 */
public class NissaSageAnimist extends Card {

    public NissaSageAnimist() {
        // +1: Reveal the top card of your library. If it's a land card, put it onto the battlefield.
        //     Otherwise, put it into your hand.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RevealTopCardLandToBattlefieldElseToHandEffect()),
                "+1: Reveal the top card of your library. If it's a land card, put it onto the "
                        + "battlefield. Otherwise, put it into your hand."
        ));

        // −2: Create Ashaya, the Awoken World, a legendary 4/4 green Elemental creature token.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(CardType.CREATURE, 1, "Ashaya, the Awoken World", 4, 4,
                        CardColor.GREEN, null, List.of(CardSubtype.ELEMENTAL),
                        Set.of(), Set.of(),
                        false, false, Map.of(), List.of(), false, false, true, 0, Set.of())),
                "−2: Create Ashaya, the Awoken World, a legendary 4/4 green Elemental creature token."
        ));

        // −7: Untap up to six target lands. They become 6/6 Elemental creatures. They're still lands.
        // The animation has no duration, so it uses EffectDuration.PERMANENT over the whole target group.
        TargetFilter land = TargetFilters.land();
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS),
                        new AnimatePermanentsEffect(6, 6, List.of(CardSubtype.ELEMENTAL),
                                Set.<Keyword>of(), null, Set.of(),
                                GrantScope.TARGET, EffectDuration.PERMANENT)),
                "−7: Untap up to six target lands. They become 6/6 Elemental creatures. "
                        + "They're still lands.",
                null, -7, null, null,
                List.of(land, land, land, land, land, land),
                0, 6
        ));
    }
}
