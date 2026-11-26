package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
                        Subjects.eachOpponent(),
                        List.of(),
                        List.of(
                                new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                        DealDamageEffect.to(Subjects.you(), 1))))),
                "+2: Each opponent gets an emblem with \"" + EMBLEM_TEXT + "\"."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(DealDamageEffect.toEachCreature(3,
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL)))),
                "\u22123: Chandra, Awakened Inferno deals 3 damage to each non-Elemental creature."
        ));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(DealDamageEffect.toTargetCreatureOrPlaneswalker(new XValue()).exilingPermanentsDealtDamage()),
                "\u2212X: Chandra, Awakened Inferno deals X damage to target creature or planeswalker. "
                        + "If a permanent dealt damage this way would die this turn, exile it instead.",
                null
        ));
    }
}
