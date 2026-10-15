package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "HOC", collectorNumber = "190")
public class ShadowOfTheEnemy extends Card {

    public ShadowOfTheEnemy() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                true, creature, false, false, 0, null, false, false, false, true));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING, creature,
                null, false, true, false));
    }
}
