package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

public class DrudgeSkeletons extends Card {

    public DrudgeSkeletons() {
        super("Drudge Skeletons", CardType.CREATURE, "{1}{B}", CardColor.BLACK);

        setSubtypes(List.of(CardSubtype.SKELETON));
        setCardText("{B}: Regenerate Drudge Skeletons.");
        setPower(1);
        setToughness(1);
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), false, "{B}: Regenerate Drudge Skeletons."));
    }
}
