package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "196")
@CardRegistration(set = "GK1", collectorNumber = "9")
public class CircuDimirLobotomist extends Card {

    public CircuDimirLobotomist() {
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");

        target(anyPlayer).addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_PLAYER));
        target(anyPlayer).addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_PLAYER));
        addStaticEffect(new CantCastSpellsWithSameNameAsExiledCardEffect(true));
    }
}
