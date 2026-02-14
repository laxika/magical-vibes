package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;

import java.util.List;
import java.util.Set;

public class Scalpelexis extends Card {

    public Scalpelexis() {
        super("Scalpelexis", CardType.CREATURE, "{4}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.BEAST));
        setCardText("Flying\nWhenever Scalpelexis deals combat damage to a player, that player exiles the top four cards of their library. If two or more of those cards have the same name, repeat this process.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(1);
        setToughness(5);
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileTopCardsRepeatOnDuplicateEffect(4));
    }
}
