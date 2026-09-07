package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "559")
public class JudgmentBolt extends Card {

    public JudgmentBolt() {
        PermanentCount equipmentCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
        addEffect(EffectSlot.SPELL,
                new DealDamageToPlayersEffect(equipmentCount, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
