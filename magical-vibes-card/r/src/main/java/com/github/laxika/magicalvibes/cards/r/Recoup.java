package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "216")
@CardRegistration(set = "DDK", collectorNumber = "63")
public class Recoup extends Card {

    public Recoup() {
        addSpellEffect(new GrantFlashbackToTargetGraveyardCardEffect(
                Set.of(CardType.SORCERY)));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
