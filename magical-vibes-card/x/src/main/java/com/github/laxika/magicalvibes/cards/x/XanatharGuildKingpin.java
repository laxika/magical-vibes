package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastSpellTypesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.EnumSet;

@CardRegistration(set = "AFR", collectorNumber = "239")
@CardRegistration(set = "SLD", collectorNumber = "1785")
public class XanatharGuildKingpin extends Card {

    public XanatharGuildKingpin() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new TargetPlayerCantCastSpellTypesThisTurnEffect(EnumSet.allOf(CardType.class)))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect());
    }
}
