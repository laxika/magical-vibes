package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "170")
public class LlanowarLoamspeaker extends Card {

    public LlanowarLoamspeaker() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AnimatePermanentsEffect(
                        3, 3,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.HASTE),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN)),
                "{T}: Target land you control becomes a 3/3 Elemental creature with haste until end of turn. "
                        + "It's still a land. Activate only as a sorcery.",
                TargetFilters.landYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
