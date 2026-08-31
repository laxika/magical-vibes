package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "303")
public class WirewoodPride extends Card {

    public WirewoodPride() {
        PermanentCount elvesOnTheBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.ANY_PLAYER);
        target(TargetFilters.creature()).addEffect(
                EffectSlot.SPELL, new BoostTargetCreatureEffect(elvesOnTheBattlefield, elvesOnTheBattlefield));
    }
}
