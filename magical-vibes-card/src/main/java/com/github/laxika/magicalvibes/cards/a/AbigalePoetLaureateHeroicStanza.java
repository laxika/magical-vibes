package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HeroicStanza;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Abigale, Poet Laureate // Heroic Stanza (SOS 170).
 * <p>
 * Front face — 2/3 Legendary Creature with Flying (auto-loaded from Scryfall keywords) and:
 * "Whenever you cast a creature spell, Abigale becomes prepared." While prepared, a copy of her
 * prepare spell {@link HeroicStanza} sits in exile and may be cast; casting it unprepares her.
 */
@CardRegistration(set = "SOS", collectorNumber = "170")
public class AbigalePoetLaureateHeroicStanza extends Card {

    public AbigalePoetLaureateHeroicStanza() {
        HeroicStanza prepareSpell = new HeroicStanza();
        prepareSpell.setSetCode(getSetCode());
        prepareSpell.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(prepareSpell);

        // Whenever you cast a creature spell, Abigale becomes prepared.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new BecomePreparedEffect())
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HeroicStanza";
    }
}
