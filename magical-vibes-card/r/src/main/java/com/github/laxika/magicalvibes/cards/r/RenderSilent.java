package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "DGM", collectorNumber = "96")
@CardRegistration(set = "GK2", collectorNumber = "18")
@CardRegistration(set = "PIO", collectorNumber = "394")
public class RenderSilent extends Card {

    public RenderSilent() {
        // Counter target spell. Its controller can't cast spells this turn.
        //
        // The counter comes first: a replacement of it (Guile) lets its controller cast the spell
        // before the restriction applies, which then uses the controller recorded by the counter.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(PlayerRestrictionEffect.thisTurn(
                Subjects.controllerOf(Subjects.targetSpell()), PlayerRestriction.castingSpells()));
    }
}
