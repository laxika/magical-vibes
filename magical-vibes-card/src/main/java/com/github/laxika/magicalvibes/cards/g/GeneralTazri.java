package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "19")
public class GeneralTazri extends Card {

    public GeneralTazri() {
        PermanentHasSubtypePredicate ally = new PermanentHasSubtypePredicate(CardSubtype.ALLY);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ALLY)),
                "Search your library for an Ally creature card?"));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(new BoostAllOwnCreaturesEffect(
                        new ColorsAmongControlledPermanents(ally),
                        new ColorsAmongControlledPermanents(ally),
                        ally)),
                "Ally creatures you control get +X/+X until end of turn, where X is the number of colors among those creatures."
        ));
    }
}
