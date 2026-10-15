package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "185")
public class AngrathsRampage extends Card {

    public AngrathsRampage() {
        PlayerPredicateTargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player sacrifices an artifact of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(),
                                SacrificeRecipient.TARGET_PLAYER),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player sacrifices a creature of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.TARGET_PLAYER),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player sacrifices a planeswalker of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsPlaneswalkerPredicate(),
                                SacrificeRecipient.TARGET_PLAYER),
                        anyPlayer)
        )));
    }
}
