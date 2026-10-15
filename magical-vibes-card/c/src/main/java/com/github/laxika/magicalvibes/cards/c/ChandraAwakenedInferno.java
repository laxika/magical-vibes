package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "127")
public class ChandraAwakenedInferno extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your upkeep, this emblem deals 1 damage to you.";

    public ChandraAwakenedInferno() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.UPKEEP,
                                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT,
                        EmblemRecipient.EACH_OPPONENT)),
                "+2: Each opponent gets an emblem with \"" + EMBLEM_TEXT + "\"."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new MassDamageEffect(
                        3, false, false, false,
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL)))),
                "\u22123: Chandra, Awakened Inferno deals 3 damage to each non-Elemental creature."
        ));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue(), null, true)),
                "\u2212X: Chandra, Awakened Inferno deals X damage to target creature or planeswalker. "
                        + "If a permanent dealt damage this way would die this turn, exile it instead.",
                null
        ));
    }
}
