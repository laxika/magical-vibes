package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "114")
public class CaseyRaphHotheads extends Card {

    public CaseyRaphHotheads() {
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(
                List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Target player exiles the top card of their library and may play it without paying its mana cost until their next end step",
                                new ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(
                                        1, LibraryScope.TARGET_PLAYER, true),
                                anyPlayer),
                        new ChooseOneEffect.ChooseOneOption(
                                "Target player creates two Treasure tokens",
                                new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofTreasureToken(2)),
                                anyPlayer)
                ), false, 1, 2, false)));
    }
}
