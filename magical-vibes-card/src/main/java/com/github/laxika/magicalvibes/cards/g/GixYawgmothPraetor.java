package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "95")
public class GixYawgmothPraetor extends Card {

    public GixYawgmothPraetor() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new MayPayManaEffect("{0}", 1, new DrawCardEffect(1),
                                "Pay 1 life to draw a card?")));

        addEffect(EffectSlot.STATIC,
                new AllowCastFromCardsExiledWithSourceEffect(
                        false, null, false, false, 0, null, false, false, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}{B}",
                List.of(
                        new DiscardXCardsCost(),
                        new ExileTopCardsToSourceEffect(
                                new XValue(), false, false, LibraryScope.TARGET_OPPONENT, true)),
                "{4}{B}{B}{B}, Discard X cards: Exile the top X cards of target opponent's library. "
                        + "You may play lands and cast spells from among cards exiled this way without "
                        + "paying their mana costs.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent"))
                .withXValue());
    }
}
