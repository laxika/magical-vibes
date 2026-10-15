package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "47")
@CardRegistration(set = "TDC", collectorNumber = "87")
public class RampartArchitect extends Card {

    public RampartArchitect() {
        CreateTokenEffect wallToken = new CreateTokenEffect(
                "Wall", 1, 3, CardColor.WHITE, List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, wallToken);
        addEffect(EffectSlot.ON_ATTACK, wallToken);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.DEFENDER),
                new MayEffect(new SearchLibraryEffect(
                        CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a basic land card?")));
    }
}
