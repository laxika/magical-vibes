package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsCantBeCounteredEffect;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "59")
@CardRegistration(set = "DBL", collectorNumber = "59")
@CardRegistration(set = "SLD", collectorNumber = "2350")
@CardRegistration(set = "TDC", collectorNumber = "157")
public class LierDiscipleOfTheDrowned extends Card {

    public LierDiscipleOfTheDrowned() {
        addStaticEffect(new SpellsCantBeCounteredEffect());
        addStaticEffect(new GrantFlashbackToGraveyardCardsEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
