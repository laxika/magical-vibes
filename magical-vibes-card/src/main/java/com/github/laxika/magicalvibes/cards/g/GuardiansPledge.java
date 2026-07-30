package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "22")
public class GuardiansPledge extends Card {

    public GuardiansPledge() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(
                2, 2, new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
