package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "97")
public class SaprazzanBailiff extends Card {

    public SaprazzanBailiff() {
        CardPredicate artifactOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.ENCHANTMENT)
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.ALL_PLAYERS, artifactOrEnchantment));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new EachPlayerReturnsCardsFromGraveyardToHandEffect(Integer.MAX_VALUE, artifactOrEnchantment));
    }
}
