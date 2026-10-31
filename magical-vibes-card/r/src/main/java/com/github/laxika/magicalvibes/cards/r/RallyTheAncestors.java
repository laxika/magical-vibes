package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FRF", collectorNumber = "22")
@CardRegistration(set = "EA1", collectorNumber = "2")
public class RallyTheAncestors extends Card {

    public RallyTheAncestors() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .returnAll(true)
                .requiresManaValueAtMostX(true)
                .exileAtNextUpkeep(true)
                .build());
        addSpellEffect(new ExileSpellEffect());
    }
}
