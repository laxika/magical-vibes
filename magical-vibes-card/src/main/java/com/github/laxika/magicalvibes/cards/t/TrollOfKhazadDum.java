package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "191")
public class TrollOfKhazadDum extends Card {

    public TrollOfKhazadDum() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(3));
        addHandActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SWAMP))),
                "Swampcycling {1} ({1}, Discard this card: Search your library for a Swamp card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
