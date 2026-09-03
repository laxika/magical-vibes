package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "4")
public class ChronomanticEscape extends Card {

    public ChronomanticEscape() {
        addEffect(EffectSlot.SPELL, new CreaturesCantAttackControllerUntilNextTurnEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect(3));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(),
                "Suspend 3—{2}{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
