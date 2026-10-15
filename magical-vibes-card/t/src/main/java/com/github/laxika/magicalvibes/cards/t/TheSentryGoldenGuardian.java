package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "35")
public class TheSentryGoldenGuardian extends Card {

    public TheSentryGoldenGuardian() {
        CreateTokenEffect theVoid = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "The Void",
                5,
                5,
                CardColor.BLACK,
                null,
                List.of(CardSubtype.HORROR, CardSubtype.VILLAIN),
                Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, (CardEffect) new MustAttackEffect()),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of(),
                Set.of());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenForTargetPlayerEffect(theVoid));
    }
}
