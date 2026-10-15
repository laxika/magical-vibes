package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "174")
public class KaZarOfTheSavageLand extends Card {

    public KaZarOfTheSavageLand() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new PlayLandsFromTopOfLibraryEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, 1, "Zabu", 2, 2,
                CardColor.GREEN, null, List.of(CardSubtype.CAT), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                List.of(), false, false, true, 0, Set.of()));
    }
}
