package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ALA", collectorNumber = "199")
public class StoicAngel extends Card {

    public StoicAngel() {
        // Static: players can't untap more than one creature during their untap steps. Reuses the
        // Static Orb untap-pause machinery with a cap of one, filtered to creatures, and no
        // "while untapped" condition. Non-creatures untap normally; each untap step pauses only
        // when a player would otherwise untap more than one creature.
        addEffect(EffectSlot.STATIC, new StaticOrbEffect(1, new PermanentIsCreaturePredicate(), false));
    }
}
