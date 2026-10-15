package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "28")
public class FrontlineSage extends Card {

    public FrontlineSage() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. ON_ALLY_CREATURE_ATTACKS fires per attacking ally and records the attacker as the
        // trigger's (non-targeting) target, so BoostTargetCreatureEffect boosts "that creature";
        // AttacksAlone (checked at resolution) restricts it to lone attackers.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // {U}, {T}: Draw a card, then discard a card. (Loot: draw resolves before the discard.)
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{U}, {T}: Draw a card, then discard a card."
        ));
    }
}
