package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;

import java.util.List;
import java.util.Set;

public class AvenWindreader extends Card {

    public AvenWindreader() {
        super("Aven Windreader", CardType.CREATURE, "{3}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.BIRD, CardSubtype.SOLDIER, CardSubtype.WIZARD));
        setCardText("Flying\n{1}{U}: Target player reveals the top card of their library.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(3);
        setToughness(3);
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(new RevealTopCardOfLibraryEffect()), true, "{1}{U}: Target player reveals the top card of their library."));
    }
}
