package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilAnotherEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "96")
public class SuperiorFoesOfSpiderMan extends Card {

    public SuperiorFoesOfSpiderMan() {
        // Whenever you cast a spell with mana value 4 or greater, you may exile the top card of
        // your library. If you do, you may play that card until you exile another card with this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardMinManaValuePredicate(4),
                        List.of(new ExileTopCardMayPlayUntilAnotherEffect())),
                "Exile the top card of your library?"));
    }
}
