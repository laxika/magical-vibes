package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

import java.util.List;

public class BeaconOfImmortality extends Card {

    public BeaconOfImmortality() {
        super("Beacon of Immortality", CardType.INSTANT, "{5}{W}", CardColor.WHITE);

        setCardText("Double target player's life total.\nShuffle Beacon of Immortality into its owner's library.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DoubleTargetPlayerLifeEffect());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
