package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;

import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "115")
@CardRegistration(set = "SOS", collectorNumber = "333")
public class Flashback extends Card {

    public Flashback() {
        addSpellEffect(new GrantFlashbackToTargetGraveyardCardEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
