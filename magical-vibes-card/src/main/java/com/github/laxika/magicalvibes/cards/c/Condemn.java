package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;

import java.util.List;

public class Condemn extends Card {

    public Condemn() {
        super("Condemn", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Put target attacking creature on the bottom of its owner's library. Its controller gains life equal to its toughness.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new GainLifeEqualToTargetToughnessEffect());
        addEffect(EffectSlot.SPELL, new PutTargetOnBottomOfLibraryEffect());
    }
}
