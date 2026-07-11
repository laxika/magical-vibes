package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

@CardRegistration(set = "PTK", collectorNumber = "116")
public class MaChaoWesternWarrior extends Card {

    public MaChaoWesternWarrior() {
        // Horsemanship is auto-loaded from Scryfall keywords.
        // Whenever Ma Chao attacks alone, it can't be blocked this combat.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(), new MakeCreatureUnblockableEffect(true)));
    }
}
