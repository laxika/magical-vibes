package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "131")
public class RobeOfTheArchmagi extends Card {

    private static final PermanentHasAnySubtypePredicate ARCHMAGE_CREATURE =
            new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SHAMAN, CardSubtype.WARLOCK, CardSubtype.WIZARD));

    public RobeOfTheArchmagi() {
        setAttachRestriction(ARCHMAGE_CREATURE);
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
        addActivatedAbility(new EquipActivatedAbility(
                "{1}", ARCHMAGE_CREATURE,
                "Robe of the Archmagi can be attached only to a Shaman, Warlock, or Wizard"));
    }
}
