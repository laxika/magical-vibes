package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "205")
public class AshlingsCommand extends Card {

    public AshlingsCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target Elemental you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL),
                                "Target must be an Elemental you control.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws two cards",
                        new DrawCardForTargetPlayerEffect(2),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Ashling's Command deals 2 damage to each creature target player controls",
                        new DealDamageToPermanentsTargetControlsEffect(2),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player creates two Treasure tokens",
                        new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofTreasureToken(2)),
                        anyPlayer)
        ), 2));
    }
}
