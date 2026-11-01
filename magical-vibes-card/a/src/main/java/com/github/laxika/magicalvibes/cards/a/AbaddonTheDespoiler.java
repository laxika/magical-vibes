package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;

@CardRegistration(set = "40K", collectorNumber = "2")
@CardRegistration(set = "40K", collectorNumber = "171")
@CardRegistration(set = "40K", collectorNumber = "178")
@CardRegistration(set = "40K", collectorNumber = "319")
public class AbaddonTheDespoiler extends Card {

    public AbaddonTheDespoiler() {
        addStaticEffect(new GrantCastTriggerToSpellsEffect(
                SpellCastTriggerGrant.CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST, new CascadeEffect()));
    }
}
