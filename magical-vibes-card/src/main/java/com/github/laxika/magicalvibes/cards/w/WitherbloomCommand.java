package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "248")
public class WitherbloomCommand extends Card {

    public WitherbloomCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var noncreatureNonlandManaValueTwoOrLess = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentMaxManaValuePredicate(2)));
        var noncreatureNonlandManaValueTwoOrLessTarget = new PermanentPredicateTargetFilter(
                noncreatureNonlandManaValueTwoOrLess,
                "Target must be a noncreature, nonland permanent with mana value 2 or less.");
        var creature = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player mills three cards, then you return a land card from your graveyard to your hand",
                        List.of(
                                new MillEffect(3, MillRecipient.TARGET_PLAYER),
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardTypePredicate(CardType.LAND))
                                        .build()),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target noncreature, nonland permanent with mana value 2 or less",
                        new DestroyTargetPermanentEffect(),
                        noncreatureNonlandManaValueTwoOrLessTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -3/-1 until end of turn",
                        new BoostTargetCreatureEffect(-3, -1),
                        creature),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses 2 life and you gain 2 life",
                        List.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                                new GainLifeEffect(2)),
                        opponent)
        ), 2));
    }
}
