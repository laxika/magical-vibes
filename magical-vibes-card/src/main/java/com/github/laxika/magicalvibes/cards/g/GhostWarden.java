package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

public class GhostWarden extends Card {

    public GhostWarden() {
        super("Ghost Warden", CardType.CREATURE, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.SPIRIT));
        setCardText("{T}: Target creature gets +1/+1 until end of turn.");
        setPower(1);
        setToughness(1);
        setTapActivatedAbilityEffects(List.of(new BoostTargetCreatureEffect(1, 1)));
    }
}
