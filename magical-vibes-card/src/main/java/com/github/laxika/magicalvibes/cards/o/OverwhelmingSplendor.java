package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "HOU", collectorNumber = "19")
public class OverwhelmingSplendor extends Card {

    public OverwhelmingSplendor() {
        // Enchant player auto-detected from the CURSE subtype (isEnchantPlayer()).
        // Creatures enchanted player controls lose all abilities and have base power and toughness 1/1.
        addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_PLAYER_CREATURES));
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_PLAYER_CREATURES));
        // Enchanted player can't activate abilities that aren't mana abilities or loyalty abilities.
        addEffect(EffectSlot.STATIC, new EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect());
    }
}
