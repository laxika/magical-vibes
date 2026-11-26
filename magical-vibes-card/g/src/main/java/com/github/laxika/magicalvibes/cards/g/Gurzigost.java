package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "126")
public class Gurzigost extends Card {

    public Gurzigost() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay(
                new PutCardsFromGraveyardOnBottomOfLibraryCost(2),
                new SacrificeSelfEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new MayEffect(
                                new GrantEffectToTargetUntilEndOfTurnEffect(StaticAbility.of(new AssignCombatDamageAsThoughUnblockedEffect()), Subjects.source()),
                                "Have Gurzigost assign its combat damage as though it weren't blocked?")),
                "{G}{G}, Discard a card: You may have Gurzigost assign its combat damage this turn as "
                        + "though it weren't blocked."
        ));
    }
}
