package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachLeavingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "177")
@CardRegistration(set = "TMT", collectorNumber = "277")
public class TheOoze extends Card {

    public TheOoze() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new CreateTokensForEachLeavingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE, mutagenToken()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD),
                        mutagenToken()
                ),
                "{T}: Exile target card from a graveyard. Create a Mutagen token. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static CreateTokenEffect mutagenToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Mutagen", List.of(), List.of(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(
                                new SacrificeSelfCost(),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
                        ),
                        "{1}, {T}, Sacrifice this token: Put a +1/+1 counter on target creature. "
                                + "Activate only as a sorcery.",
                        TargetFilters.creature(),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                )
        ));
    }
}
