package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "MID", collectorNumber = "119")
@CardRegistration(set = "DBL", collectorNumber = "119")
public class RottenReunion extends Card {

    public RottenReunion() {
        addSpellEffect(new ExileCardsFromGraveyardEffect(1, 0));
        addSpellEffect(CreateTokenEffect.blackZombieWithDecayed(1));
        addCastingOption(new FlashbackCast("{1}{B}"));
    }
}
