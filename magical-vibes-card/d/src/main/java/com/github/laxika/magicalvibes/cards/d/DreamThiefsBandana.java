package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "38")
@CardRegistration(set = "OTC", collectorNumber = "74")
public class DreamThiefsBandana extends Card {

    public DreamThiefsBandana() {
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer())
                        .exiledFaceDown().withAnyManaType().until(Duration.PERMANENT));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
