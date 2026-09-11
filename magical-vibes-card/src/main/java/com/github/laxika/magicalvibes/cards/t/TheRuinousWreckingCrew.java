package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "224")
public class TheRuinousWreckingCrew extends Card {

    public TheRuinousWreckingCrew() {
        PlayerPredicateTargetFilter opponentTargetFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent");
        SpellTarget opponentTarget = target(opponentTargetFilter);
        LoseLifeEffect loseLife = new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER);
        registerEffectTargetIndex(loseLife, opponentTarget.getIndex());

        PermanentIsTokenPredicate tokenPredicate = new PermanentIsTokenPredicate();
        PermanentPredicateTargetFilter tokenTargetFilter = new PermanentPredicateTargetFilter(
                tokenPredicate, "Target must be a token");
        SpellTarget tokenTarget = target(tokenTargetFilter);
        DestroyTargetPermanentEffect destroyToken = new DestroyTargetPermanentEffect(tokenPredicate);
        registerEffectTargetIndex(destroyToken, tokenTarget.getIndex());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Discard a card, then draw a card.", new DiscardAndDrawCardEffect()),
                        new ChooseOneEffect.ChooseOneOption(
                                "Target opponent loses 2 life.", loseLife, opponentTargetFilter),
                        new ChooseOneEffect.ChooseOneOption(
                                "Destroy target token.", destroyToken, tokenTargetFilter),
                        new ChooseOneEffect.ChooseOneOption(
                                "Each player sacrifices a creature of their choice.",
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                        SacrificeRecipient.EACH_PLAYER))
                )), new XValue()));
    }
}
