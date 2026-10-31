package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MIR", collectorNumber = "108")
@CardRegistration(set = "BTD", collectorNumber = "19")
public class BoneHarvest extends Card {

    public BoneHarvest() {
        addSpellEffect(new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
