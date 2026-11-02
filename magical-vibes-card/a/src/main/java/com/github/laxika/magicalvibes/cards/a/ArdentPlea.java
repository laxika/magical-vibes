package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "1")
public class ArdentPlea extends Card {

    public ArdentPlea() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Same wiring as Akrasan Squire — Triggers.creatureYouControlAttacks() records the lone attacker
        // as the (non-targeting) trigger target and AttacksAlone restricts it to solo attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
