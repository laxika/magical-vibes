package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "13")
public class CommandersAuthority extends Card {

    public CommandersAuthority() {
        // Enchant creature. Enchanted creature has "At the beginning of your upkeep, create a
        // 1/1 white Human creature token." The granted ability belongs to the creature, so it
        // triggers on the enchanted creature's controller's upkeep and the token is theirs —
        // ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED bakes that player as the stack targetId,
        // which CreateTokenForTargetPlayerEffect creates the token for.
        target(TargetFilters.creature()).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                        "Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN),
                        Set.<Keyword>of(), Set.<CardType>of())));
    }
}
