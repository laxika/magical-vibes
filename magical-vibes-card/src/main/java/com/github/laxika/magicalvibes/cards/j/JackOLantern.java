package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "254")
public class JackOLantern extends Card {

    public JackOLantern() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new ExileCardsFromGraveyardEffect(1, 0),
                        new DrawCardEffect()
                ),
                "{1}, {T}, Sacrifice this artifact: Exile up to one target card from a graveyard. Draw a card."
        ));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ExileSelfFromGraveyardCost(), new AwardAnyColorManaEffect()),
                "{1}, Exile this card from your graveyard: Add one mana of any color."
        ));
    }
}
