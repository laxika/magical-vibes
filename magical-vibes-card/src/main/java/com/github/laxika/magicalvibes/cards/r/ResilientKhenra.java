package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "131")
public class ResilientKhenra extends Card {

    public ResilientKhenra() {
        // When this creature enters, you may have target creature get +X/+X until end of turn,
        // where X is this creature's power. SourcePower reads the entering permanent's effective
        // power (its Eternalize token is a 4/4 → +4/+4).
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new BoostTargetCreatureEffect(new SourcePower(), new SourcePower()),
                "Have target creature get +X/+X until end of turn, where X is this creature's power?"
        ));

        // Eternalize {4}{G}{G} ({4}{G}{G}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Jackal Wizard with no mana cost. Eternalize only
        // as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)
                ),
                "Eternalize {4}{G}{G} ({4}{G}{G}, Exile this card from your graveyard: Create a token that's a "
                        + "copy of it, except it's a 4/4 black Zombie Jackal Wizard with no mana cost. Eternalize "
                        + "only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
