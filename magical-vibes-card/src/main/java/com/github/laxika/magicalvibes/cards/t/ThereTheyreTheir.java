package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "301")
@CardRegistration(set = "MB2", collectorNumber = "537")
public class ThereTheyreTheir extends Card {

    public ThereTheyreTheir() {
        TargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "There - Exile target creature you control, then return it to the battlefield under its owner's control",
                        FlickerEffect.flickerTarget(),
                        TargetFilters.creatureYouControl()
                ).withManaCost("{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "They're - Up to three target creatures can't be blocked this turn",
                        List.<CardEffect>of(new MakeCreatureUnblockableEffect()),
                        TargetFilters.creature(), null, 0, 3, false, null
                ).withManaCost("{1}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Their - Target opponent gains control of target creature you control",
                        List.of(new TargetPlayerGainsControlOfTargetPermanentEffect()),
                        List.of(opponent, TargetFilters.creatureYouControl())
                ).withManaCost("{2}{U}")
        )));
    }
}
