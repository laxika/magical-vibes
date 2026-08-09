package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "143")
public class TerrainGenerator extends Card {

    public TerrainGenerator() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(CardPredicateUtils.basicLand(), "basic land", true),
                        "Put a basic land card from your hand onto the battlefield tapped?"
                )),
                "{2}, {T}: You may put a basic land card from your hand onto the battlefield tapped."
        ));
    }
}
