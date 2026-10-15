package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "90")
@CardRegistration(set = "FIC", collectorNumber = "180")
public class PapalymoTotolymo extends Card {

    public PapalymoTotolymo() {
        // Whenever you cast a noncreature spell, this creature deals 1 damage to each opponent and
        // you gain 1 life.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)
                )
        ));

        // {4}, {T}, Sacrifice this creature: Each opponent who lost life this turn sacrifices a
        // creature with the greatest power among creatures they control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new SacrificeSelfCost(),
                        new SacrificePermanentsEffect(
                                new FixedIfCondition(new ControllerLostLifeThisTurn(1), 1, 0),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasGreatestPowerAmongControllerCreaturesPredicate()
                                )),
                                SacrificeRecipient.EACH_OPPONENT,
                                true
                        )
                ),
                "{4}, {T}, Sacrifice Papalymo Totolymo: Each opponent who lost life this turn "
                        + "sacrifices a creature with the greatest power among creatures they control."
        ));
    }
}
