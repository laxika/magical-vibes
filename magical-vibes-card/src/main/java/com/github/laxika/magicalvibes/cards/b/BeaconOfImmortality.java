package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;

import java.util.List;

public class BeaconOfImmortality extends Card {

    public BeaconOfImmortality() {
        super("Beacon of Immortality", CardType.INSTANT, "{5}{W}", CardColor.WHITE);

        setCardText("Double target player's life total.\nShuffle Beacon of Immortality into its owner's library.");
        setNeedsTarget(true);
        setShuffleIntoLibraryOnResolve(true);
        setSpellEffects(List.of(new DoubleTargetPlayerLifeEffect()));
    }
}
