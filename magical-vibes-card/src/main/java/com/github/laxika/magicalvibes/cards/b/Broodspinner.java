package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "211")
public class Broodspinner extends Card {

    public Broodspinner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{B}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new CardTypesAmongCardsInGraveyard(),
                                "Insect",
                                1,
                                1,
                                CardColor.BLACK,
                                Set.of(CardColor.BLACK, CardColor.GREEN),
                                List.of(CardSubtype.INSECT),
                                Set.of(Keyword.FLYING),
                                Set.of(),
                                false,
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        )
                ),
                "{4}{B}{G}, {T}, Sacrifice this creature: Create a number of 1/1 black and green Insect creature tokens with flying equal to the number of card types among cards in your graveyard."
        ));
    }
}
