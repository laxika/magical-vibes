package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "127")
public class ChandraAwakenedInferno extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your upkeep, this emblem deals 1 damage to you.";

    public ChandraAwakenedInferno() {
        addStaticEffect(new CantBeCounteredEffect());

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new CreateEmblemEffect(
                        List.of(),
                        EMBLEM_TEXT,
                        EmblemRecipient.EACH_OPPONENT,
                        List.of(),
                        List.of(
                                new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))))),
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
                List.of(DealDamageToTargetCreatureOrPlaneswalkerEffect.exilingIfDealtDamage(new XValue())),
                "\u2212X: Chandra, Awakened Inferno deals X damage to target creature or planeswalker. "
                        + "If a permanent dealt damage this way would die this turn, exile it instead.",
                null
        ));
    }
}
