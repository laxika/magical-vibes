package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "40")
public class AbsorbVis extends Card {

    public AbsorbVis() {
        // Target player loses 4 life and you gain 4 life.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(4, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));

        // Basic landcycling {1}{B} ({1}{B}, Discard this card: Search your library for a basic land
        // card, reveal it, put it into your hand, then shuffle.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{B} ({1}{B}, Discard this card: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.)"));
    }
}
