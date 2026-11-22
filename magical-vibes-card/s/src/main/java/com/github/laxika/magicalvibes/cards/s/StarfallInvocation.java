package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BLB", collectorNumber = "34")
public class StarfallInvocation extends Card {

    public StarfallInvocation() {
        addStaticEffect(new GiftEffect());
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                new EachOtherPlayerDrawsCardEffect(1)));
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                SequenceEffect.of(
                        ChooseCardEffect.announced(Subjects.inYourGraveyard(Subjects.cardsThisWay(StepKind.DESTROY,
                                new CardTypePredicate(CardType.CREATURE))), "a creature card put into your graveyard this way"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .underControlOf(Subjects.you()))));
    }
}
