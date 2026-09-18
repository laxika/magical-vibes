package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "10")
@CardRegistration(set = "ECC", collectorNumber = "30")
public class Lamentation extends Card {

    public Lamentation() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{6}{B}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopiesOfSourceAttackingOpponentsEffect()
                ),
                "Encore {6}{B}{B} ({6}{B}{B}, Exile this card from your graveyard: For each opponent, create a token copy "
                        + "that attacks that opponent this turn if able. They gain haste. Sacrifice them at the beginning "
                        + "of the next end step. Activate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
