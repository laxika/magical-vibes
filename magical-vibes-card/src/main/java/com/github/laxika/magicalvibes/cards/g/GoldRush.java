package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "166")
public class GoldRush extends Card {

    public GoldRush() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));

        PermanentCount treasures = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.TREASURE), CountScope.CONTROLLER);
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                        new Scaled(treasures, 2), new Scaled(treasures, 2)));
    }
}
