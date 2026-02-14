package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactFromGraveyardToHandEffect;

import java.util.List;

public class TreasureHunter extends Card {

    public TreasureHunter() {
        super("Treasure Hunter", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN));
        setCardText("When Treasure Hunter enters, you may return target artifact card from your graveyard to your hand.");
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new ReturnArtifactFromGraveyardToHandEffect(), "Return an artifact from your graveyard to your hand?"));
    }
}
