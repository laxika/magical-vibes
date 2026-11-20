package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HOU", collectorNumber = "19")
@CardRegistration(set = "AKR", collectorNumber = "30")
public class OverwhelmingSplendor extends Card {

    public OverwhelmingSplendor() {
        // Enchant player auto-detected from the CURSE subtype (isEnchantPlayer()).
        // Creatures enchanted player controls lose all abilities and have base power and toughness 1/1.
        addStaticEffect(new LosesAllAbilitiesEffect(Subjects.creaturesEnchantedPlayerControls()));
        addStaticEffect(new SetBasePowerToughnessEffect(1, 1, Subjects.creaturesEnchantedPlayerControls()));
        // Enchanted player can't activate abilities that aren't mana abilities or loyalty abilities.
        addStaticEffect(new EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect());
    }
}
