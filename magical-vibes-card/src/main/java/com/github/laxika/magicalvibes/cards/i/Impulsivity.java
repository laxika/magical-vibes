package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "13")
@CardRegistration(set = "ECC", collectorNumber = "33")
public class Impulsivity extends Card {

    public Impulsivity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.ALL_GRAVEYARDS, true, true));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{7}{R}{R}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopiesOfSourceAttackingOpponentsEffect()
                ),
                "Encore {7}{R}{R} ({7}{R}{R}, Exile this card from your graveyard: For each opponent, create a token copy "
                        + "that attacks that opponent this turn if able. They gain haste. Sacrifice them at the beginning "
                        + "of the next end step. Activate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
