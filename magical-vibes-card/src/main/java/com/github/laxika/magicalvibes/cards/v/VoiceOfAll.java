package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;

import java.util.List;
import java.util.Set;

public class VoiceOfAll extends Card {

    public VoiceOfAll() {
        super("Voice of All", CardType.CREATURE, "{2}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.ANGEL));
        setCardText("Flying\nAs Voice of All enters the battlefield, choose a color. Voice of All has protection from the chosen color.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProtectionFromChosenColorEffect());
    }
}
