package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesAllUnspentManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "87")
@CardRegistration(set = "5ED", collectorNumber = "111")
public class PowerSink extends Card {

    public PowerSink() {
        // Counter target spell unless its controller pays {X}. If that player doesn't, they tap all
        // lands with mana abilities they control and lose all unspent mana.
        List<CardEffect> ifNotPaid = List.of(
                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, new PermanentIsLandPredicate()),
                new TargetPlayerLosesAllUnspentManaEffect());
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false, ifNotPaid));
    }
}
