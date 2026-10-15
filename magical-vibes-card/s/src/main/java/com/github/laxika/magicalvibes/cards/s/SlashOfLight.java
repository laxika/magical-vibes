package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FIN", collectorNumber = "32")
public class SlashOfLight extends Card {

    public SlashOfLight() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new Sum(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER)
        )));
    }
}
