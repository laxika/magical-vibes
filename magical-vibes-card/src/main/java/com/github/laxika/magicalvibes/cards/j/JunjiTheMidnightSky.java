package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "102")
public class JunjiTheMidnightSky extends Card {

    public JunjiTheMidnightSky() {
        var nonDragonCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.DRAGON))));
        var returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(nonDragonCreature)
                .targetGraveyard(true)
                .build();

        addEffect(EffectSlot.ON_DEATH, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent discards two cards and loses 2 life",
                        List.of(
                                new DiscardEffect(2, DiscardRecipient.EACH_OPPONENT),
                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Put target non-Dragon creature card from a graveyard onto the battlefield under your control. You lose 2 life",
                        List.of(returnCreature, new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER)))
        )));
    }
}
