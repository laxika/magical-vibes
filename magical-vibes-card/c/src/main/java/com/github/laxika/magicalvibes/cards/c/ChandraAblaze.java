package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CastCardsWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "120")
public class ChandraAblaze extends Card {

    public ChandraAblaze() {
        CardPredicate redCard = new CardColorPredicate(CardColor.RED);
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD, redCard),
                                new DealDamageToAnyTargetEffect(4))),
                "+1: Discard a card. If a red card is discarded this way, Chandra deals 4 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new DiscardHandEffect(Subjects.eachPlayer()),
                        new EachPlayerDrawsCardEffect(3)),
                "−2: Each player discards their hand, then draws three cards."
        ));

        CardPredicate redInstantOrSorcery = new CardAllOfPredicate(List.of(
                redCard,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))
        ));
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(CastCardsWithoutPayingManaCostEffect.anyNumber(
                        Subjects.cardsInGraveyards(Subjects.you(), redInstantOrSorcery))),
                "−7: Cast any number of red instant and/or sorcery cards from your graveyard without paying their mana costs."
        ));
    }
}
