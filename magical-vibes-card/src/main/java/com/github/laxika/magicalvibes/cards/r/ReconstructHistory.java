package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "222")
public class ReconstructHistory extends Card {

    public ReconstructHistory() {
        CardPredicate historicCard = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY),
                new CardTypePredicate(CardType.PLANESWALKER)));

        addEffect(EffectSlot.SPELL, ReturnTargetCardsFromGraveyardToHandEffect.upToOnePerCardType(
                historicCard, Set.of(
                        CardType.ARTIFACT,
                        CardType.ENCHANTMENT,
                        CardType.INSTANT,
                        CardType.SORCERY,
                        CardType.PLANESWALKER)));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
