package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "336")
@CardRegistration(set = "C20", collectorNumber = "222")
@CardRegistration(set = "C16", collectorNumber = "38")
public class MigratoryRoute extends Card {

    public MigratoryRoute() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                4, "Bird", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {2} ({2}, Discard this card: Search your library for a basic land card, "
                        + "reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
