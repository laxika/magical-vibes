package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapOtherCreaturesForUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "82")
public class TidalTerror extends Card {

    public TidalTerror() {
        addEffect(EffectSlot.ON_ATTACK, new TapOtherCreaturesForUnblockableEffect(2));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ISLAND))),
                "Islandcycling {2} ({2}, Discard this card: Search your library for an Island card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
