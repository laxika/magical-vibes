package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateDiscardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "101")
public class CollectiveBrutality extends Card {

    public CollectiveBrutality() {
        // Escalate—Discard a card. (Pay this cost for each mode chosen beyond the first.)
        addEffect(EffectSlot.SPELL, new EscalateDiscardCost());

        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        // Choose one or more —
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent reveals their hand. You choose an instant or sorcery card from it. That player discards that card",
                        new ChooseCardsFromTargetHandEffect(
                                1, List.of(), List.of(CardType.INSTANT, CardType.SORCERY),
                                HandChoiceDestination.DISCARD),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -2/-2 until end of turn",
                        new BoostTargetCreatureEffect(-2, -2),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses 2 life and you gain 2 life",
                        List.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                                new GainLifeEffect(2)),
                        opponentFilter)
        )));
    }
}
