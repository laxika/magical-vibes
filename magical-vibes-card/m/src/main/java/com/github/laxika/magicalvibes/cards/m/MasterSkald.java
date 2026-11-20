package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "19")
public class MasterSkald extends Card {

    public MasterSkald() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(SequenceEffect.of(
                ExileGraveyardCardsEffect.fromOwnGraveyard(1, new CardTypePredicate(CardType.CREATURE)),
                new ConditionalEffect(HappenedThisWay.exiled(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.ENCHANTMENT))))
                        .targetGraveyard(true)
                        .build())),
                "Exile a creature card from your graveyard?"));
    }
}
