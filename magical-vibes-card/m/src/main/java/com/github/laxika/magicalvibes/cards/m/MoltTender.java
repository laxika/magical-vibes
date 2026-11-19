package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DFT", collectorNumber = "171")
public class MoltTender extends Card {

    public MoltTender() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(1, Subjects.you())),
                "{T}: Mill a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileCardFromGraveyardCost((CardType) null),
                        new AwardAnyColorManaEffect()
                ),
                "{T}, Exile a card from your graveyard: Add one mana of any color."
        ));
    }
}
