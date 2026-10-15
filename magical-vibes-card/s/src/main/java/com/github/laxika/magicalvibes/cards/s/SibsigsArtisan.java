package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "9")
public class SibsigsArtisan extends Card {

    public SibsigsArtisan() {
        addGraveyardActivatedAbility(renewAbility());
    }

    private static ActivatedAbility renewAbility() {
        List<CardEffect> effects = new ArrayList<>();
        ActivatedAbility ability = new ActivatedAbility(
                false,
                "{1}{B}{B}",
                effects,
                "Renew {1}{B}{B} ({1}{B}{B}, Exile this card from your graveyard: Put three +1/+1 counters "
                        + "on target creature you control. It perpetually gains this ability. Activate only as a sorcery.)",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED);
        effects.add(new ExileSelfFromGraveyardCost());
        effects.add(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
        effects.add(new PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect(ability));
        return ability;
    }
}
