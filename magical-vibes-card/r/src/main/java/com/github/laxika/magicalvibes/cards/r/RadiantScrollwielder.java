package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "221")
public class RadiantScrollwielder extends Card {

    public RadiantScrollwielder() {
        addStaticEffect(GrantLifelinkToControllerSpellsByColorEffect.allColors());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                ExileCardAtRandomEffect.fromGraveyard(Subjects.you(), new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))),
                AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))
                        .exilingInsteadOfGraveyard()));
    }
}
