package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "29")
public class RelicSeeker extends Card {

    public RelicSeeker() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));

        // When Relic Seeker becomes renowned, you may search your library for an Equipment card,
        // reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.ON_SELF_BECOMES_RENOWNED,
                new MayEffect(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)),
                        "Search your library for an Equipment card?"));
    }
}
