package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "WWK", collectorNumber = "132")
@CardRegistration(set = "SLD", collectorNumber = "1532")
@CardRegistration(set = "HA2", collectorNumber = "20")
@CardRegistration(set = "TSR", collectorNumber = "406")
@CardRegistration(set = "C13", collectorNumber = "278")
@CardRegistration(set = "CMD", collectorNumber = "267")
@CardRegistration(set = "C14", collectorNumber = "285")
public class BojukaBog extends Card {

    public BojukaBog() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
