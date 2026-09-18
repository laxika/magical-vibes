package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "165")
public class HerdMigration extends Card {

    public HerdMigration() {
        // Domain — Create a 3/3 green Beast creature token for each basic land type among lands you control.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new BasicLandTypesAmongControlledLands(),
                "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand()), new GainLifeEffect(3)),
                "{1}{G}, Discard this card: Search your library for a basic land card, reveal it, put it "
                        + "into your hand, then shuffle. You gain 3 life."
        ));
    }
}
