package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "218")
public class ExperimentalOverload extends Card {

    public ExperimentalOverload() {
        DynamicAmount instantSorceryCount = new CardsInGraveyard(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        )), CountScope.CONTROLLER);

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, new Fixed(1), "Weird", instantSorceryCount, instantSorceryCount,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.WEIRD), Set.of(), Set.of(),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of()));

        addEffect(EffectSlot.SPELL, new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )))
                        .build(),
                "Return an instant or sorcery card from your graveyard to your hand?"));

        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
