package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INV", collectorNumber = "207")
public class RootingKavu extends Card {

    public RootingKavu() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                SequenceEffect.of(
                        new ExileSourceCardFromGraveyardEffect(),
                        new ShuffleGraveyardIntoLibraryEffect(false, new CardTypePredicate(CardType.CREATURE))),
                "Exile Rooting Kavu to shuffle all creature cards from your graveyard into your library?"));
    }
}
