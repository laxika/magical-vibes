package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "19")
public class WanderingTreefolk extends Card {

    public WanderingTreefolk() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}{G}",
                List.of(
                        new ReduceActivationCostEffect(new BasicLandTypesAmongControlledLands()),
                        new SeekFromLibraryToHandEffect(new CardTypePredicate(CardType.CREATURE))
                ),
                "{7}{G}: Seek a creature card. This ability costs {1} less to activate for each basic land type among lands you control."
        ));
    }
}
