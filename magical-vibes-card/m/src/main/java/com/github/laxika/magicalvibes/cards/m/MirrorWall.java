package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "47")
public class MirrorWall extends Card {

    public MirrorWall() {
        // Defender is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new CanAttackAsThoughNoDefenderEffect()),
                "{W}: This creature can attack this turn as though it didn't have defender."));
    }
}
