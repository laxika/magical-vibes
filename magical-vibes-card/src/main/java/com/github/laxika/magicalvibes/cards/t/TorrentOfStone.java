package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "119")
public class TorrentOfStone extends Card {

    public TorrentOfStone() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, List.of(
                new SacrificePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));
    }
}
