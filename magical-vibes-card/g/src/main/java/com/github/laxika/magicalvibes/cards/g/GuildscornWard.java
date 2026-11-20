package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "15")
public class GuildscornWard extends Card {

    public GuildscornWard() {
        target(TargetFilters.creature()).addStaticEffect(new ProtectionFromMulticoloredEffect(Subjects.attachedCreature()));
    }
}
