package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "78")
@CardRegistration(set = "9ED", collectorNumber = "73")
@CardRegistration(set = "8ED", collectorNumber = "75")
@CardRegistration(set = "MMQ", collectorNumber = "73")
public class Dehydration extends Card {

    public Dehydration() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
