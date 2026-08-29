package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "41")
public class KahoMinamoHistorian extends Card {

    public KahoMinamoHistorian() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForCardsToExileWithSourceEffect(
                        new CardTypePredicate(CardType.INSTANT), 3));

        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new MayCastCardExiledWithSourceEffect(new XValue())),
                "{X}, {T}: You may cast a spell with mana value X from among cards exiled with Kaho "
                        + "without paying its mana cost."));
    }
}
