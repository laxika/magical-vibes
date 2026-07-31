package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "83")
public class VarchildsWarRiders extends Card {

    public VarchildsWarRiders() {
        // Cumulative upkeep — Have an opponent create a 1/1 red Survivor creature token
        // (one per age counter).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.opponentToken(
                new CreateTokenEffect("Survivor", 1, 1, CardColor.RED,
                        List.of(CardSubtype.SURVIVOR), Set.of(), Set.of())));

        // Trample is auto-loaded from Scryfall.
        // Rampage 1: whenever this becomes blocked, it gets +1/+1 until end of turn for each
        // creature blocking it beyond the first, i.e. blockers - 1.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Sum(new CreaturesBlockingSource(), new Fixed(-1)),
                new Sum(new CreaturesBlockingSource(), new Fixed(-1))));
    }
}
