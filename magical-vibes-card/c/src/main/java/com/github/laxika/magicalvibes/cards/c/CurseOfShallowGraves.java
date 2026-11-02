package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C13", collectorNumber = "71")
public class CurseOfShallowGraves extends Card {

    public CurseOfShallowGraves() {
        CreateTokenEffect zombie = new CreateTokenEffect(
                1, "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE),
                Set.of(), Set.of(), true);
        addTrigger(Triggers.playerAttacks(),
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        new MayEffect(
                                new CreateTokenForTriggeringPlayerEffect(zombie),
                                "Create a tapped 2/2 black Zombie creature token?",
                                null,
                                MayChoicePlayer.TARGET_PLAYER)));
    }
}
