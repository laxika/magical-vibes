package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JUD", collectorNumber = "10")
public class FuneralPyre extends Card {

    public FuneralPyre() {
        // Exile target card from a graveyard. Its owner creates a 1/1 white Spirit creature token with flying.
        addSpellEffect(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false));
        addSpellEffect(CreateTokenEffect.whiteSpirit(1).forController(Subjects.playersThisWay(StepKind.EXILE)));
    }
}
