package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DreadWhispers;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "WOE", collectorNumber = "231")
public class MosswoodDreadknight extends Card {

    public MosswoodDreadknight() {
        setBackFaceCard(new DreadWhispers());
        addCastingOption(new AdventureCast("{1}{B}"));
        addEffect(EffectSlot.ON_DEATH,
                new MayEffect(new AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffect(),
                        "Cast it from your graveyard as an Adventure until the end of your next turn?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DreadWhispers";
    }
}
