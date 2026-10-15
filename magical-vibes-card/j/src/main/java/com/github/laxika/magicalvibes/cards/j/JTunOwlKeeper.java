package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "9")
public class JTunOwlKeeper extends Card {

    public JTunOwlKeeper() {
        // Cumulative upkeep {W} or {U}; each {W/U} pays one age-counter cost.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{W/U}"));

        // When this creature dies, create a 1/1 white Bird creature token with flying for each
        // age counter on it.
        addEffect(EffectSlot.ON_DEATH, new CreateTokensForEachDyingSourceCounterEffect(
                CounterType.AGE,
                new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));
    }
}
