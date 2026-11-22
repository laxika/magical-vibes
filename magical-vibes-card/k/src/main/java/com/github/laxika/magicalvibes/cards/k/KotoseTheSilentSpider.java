package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "228")
public class KotoseTheSilentSpider extends Card {

    public KotoseTheSilentSpider() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD,
                        new CardNotPredicate(CardPredicateUtils.basicLand()), true, true, false),
                ExileCardsWithSameNameEffect.anyNumber(Subjects.cardsThisWay(StepKind.EXILE)).exiledWithSource()));
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect.oneCardWithAnyMana());
    }
}
