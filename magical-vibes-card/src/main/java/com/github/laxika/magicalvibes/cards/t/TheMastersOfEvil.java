package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "105")
public class TheMastersOfEvil extends Card {

    public TheMastersOfEvil() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAN))),
                "{1}{B}, Discard this card: Search your library for a Plan card, reveal it, put it into your hand, then shuffle."));
    }
}
