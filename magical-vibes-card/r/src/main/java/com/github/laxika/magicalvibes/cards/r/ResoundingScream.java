package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "83")
public class ResoundingScream extends Card {

    public ResoundingScream() {
        // Target player discards a card at random.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true));

        // Cycling {5}{U}{B}{R} ({5}{U}{B}{R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, target player discards two cards at random." The reflexive trigger
        // rides on the cycling ability: its target player is chosen at activation, the random discard
        // resolves, then the cycling draw resumes.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{U}{B}{R}",
                List.of(new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER, true), new DrawCardEffect(1)),
                "Cycling {5}{U}{B}{R} ({5}{U}{B}{R}, Discard this card: Draw a card.)",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")));
    }
}
