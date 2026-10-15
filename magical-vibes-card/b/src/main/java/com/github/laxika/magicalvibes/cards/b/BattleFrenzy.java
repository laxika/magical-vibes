package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "175")
public class BattleFrenzy extends Card {

    public BattleFrenzy() {
        var green = new PermanentColorInPredicate(Set.of(CardColor.GREEN));

        // Green creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1, green));

        // Nongreen creatures you control get +1/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0, new PermanentNotPredicate(green)));
    }
}
