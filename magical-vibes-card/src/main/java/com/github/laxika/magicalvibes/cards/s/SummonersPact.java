package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterPayManaOrLoseGameAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "139")
public class SummonersPact extends Card {

    public SummonersPact() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.GREEN),
                new CardTypePredicate(CardType.CREATURE)
        ))));
        addEffect(EffectSlot.SPELL, new RegisterPayManaOrLoseGameAtNextUpkeepEffect("{2}{G}{G}"));
    }
}
