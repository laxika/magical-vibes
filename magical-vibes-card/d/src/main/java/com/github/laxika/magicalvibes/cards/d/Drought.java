package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AdditionalSacrificePerManaSymbolEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "21")
public class Drought extends Card {

    public Drought() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Spells / activated abilities cost an additional "Sacrifice a Swamp" for each black
        // mana symbol in their mana / activation costs.
        addStaticEffect(new AdditionalSacrificePerManaSymbolEffect(
                ManaColor.BLACK,
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                "a Swamp",
                true,
                true));
    }
}
