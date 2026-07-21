package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "112")
public class GrixisSojourners extends Card {

    public GrixisSojourners() {
        // When this creature dies, you may exile target card from a graveyard. Modelled as a
        // resolution-time, cross-graveyard "up to one" select (the optional "you may" = choose zero).
        addEffect(EffectSlot.ON_DEATH, new ExileUpToOneCardFromGraveyardEffect());

        // Cycling {2}{B} ({2}{B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may exile target card from a graveyard." The reflexive cycle
        // trigger rides on the cycling ability (Resounding Silence pattern): the graveyard exile
        // choice resolves first, then the cycling draw resumes.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{B}",
                List.of(new ExileUpToOneCardFromGraveyardEffect(), new DrawCardEffect(1)),
                "Cycling {2}{B} ({2}{B}, Discard this card: Draw a card.)"));
    }
}
