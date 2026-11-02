package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AqueousAria;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Campus Composer // Aqueous Aria (SOS 40).
 * <p>
 * Front face — 3/4 Merfolk Bard with Prepared and Ward (auto-loaded from Scryfall keywords) and:
 * "This creature enters prepared." While prepared, a copy of its prepare spell {@link AqueousAria} sits in
 * exile and may be cast; casting it unprepares it.
 */
@CardRegistration(set = "SOS", collectorNumber = "40")
public class CampusComposerAqueousAria extends Card {

    public CampusComposerAqueousAria() {
        AqueousAria prepareSpell = new AqueousAria();
        prepareSpell.setSetCode(getSetCode());
        prepareSpell.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(prepareSpell);

        // This creature enters prepared.
        addStaticEffect(new BecomePreparedEffect());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
    }

    @Override
    public String getBackFaceClassName() {
        return "AqueousAria";
    }
}
