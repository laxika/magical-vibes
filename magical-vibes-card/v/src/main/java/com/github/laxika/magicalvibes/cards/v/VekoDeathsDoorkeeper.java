package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "30")
public class VekoDeathsDoorkeeper extends Card {

    public VekoDeathsDoorkeeper() {
        // Extort.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                        "{W/B}"
                ),
                "Pay {W/B} to extort?"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))
                                )),
                                "a non-Spirit creature",
                                false
                        ),
                        new ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect(
                                CardSubtype.SPIRIT, 1, 1, "{W/B}")
                ),
                "{T}, Sacrifice a non-Spirit creature: Return target creature card from your graveyard to "
                        + "your hand. It perpetually becomes a Spirit, has base power and toughness 1/1, "
                        + "and gains \"You may pay {W/B} rather than pay this spell's mana cost.\" Activate "
                        + "only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
