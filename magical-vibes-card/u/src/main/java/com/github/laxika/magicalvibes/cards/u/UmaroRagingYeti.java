package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "63")
@CardRegistration(set = "FIC", collectorNumber = "156")
public class UmaroRagingYeti extends Card {

    private static final AnyTargetPredicateTargetFilter ANY_TARGET = new AnyTargetPredicateTargetFilter(
            new PermanentAnyOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsPlaneswalkerPredicate(),
                    new PermanentIsBattlePredicate())),
            new PlayerRelationPredicate(PlayerRelation.ANY),
            "Any target");

    public UmaroRagingYeti() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        ChooseOneEffect.ChooseOneOption pump = new ChooseOneEffect.ChooseOneOption(
                "Other creatures you control get +3/+0 and gain trample until end of turn",
                List.of(
                        new BoostAllOwnCreaturesEffect(3, 0, otherCreatures),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, otherCreatures)));
        ChooseOneEffect.ChooseOneOption discardAndDraw = new ChooseOneEffect.ChooseOneOption(
                "Discard your hand, then draw four cards",
                SequenceEffect.of(new DiscardHandEffect(), new DrawCardEffect(4)));
        ChooseOneEffect.ChooseOneOption damage = new ChooseOneEffect.ChooseOneOption(
                "Umaro deals 5 damage to any target",
                new DealDamageToAnyTargetEffect(5), ANY_TARGET);

        target(ANY_TARGET).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseOneAtRandomEffect(List.of(pump, discardAndDraw, damage)));
    }
}
