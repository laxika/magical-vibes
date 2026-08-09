package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "UDS", collectorNumber = "106")
public class Gamekeeper extends Card {

    public Gamekeeper() {
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new SourceCardInGraveyard(),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileSourceCardFromGraveyardEffect(),
                                new RevealUntilCreatureToBattlefieldRestToGraveyardEffect()),
                        "Exile Gamekeeper and reveal cards from the top of your library?")));
    }
}
