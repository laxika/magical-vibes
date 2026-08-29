package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "254")
public class ThranSpider extends Card {

    public ThranSpider() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1)))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenForTargetPlayerEffect(
                                CreateTokenEffect.ofPowerstoneToken(new Fixed(1))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4, new CardTypePredicate(CardType.ARTIFACT))),
                "{7}: Look at the top four cards of your library. You may reveal an artifact card from "
                        + "among them and put it into your hand. Put the rest on the bottom of your "
                        + "library in a random order."
        ));
    }
}
