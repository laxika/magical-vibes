package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenBlockingCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "5")
public class BrimazKingOfOreskos extends Card {

    public BrimazKingOfOreskos() {
        CreateTokenEffect attackingCat = new CreateTokenEffect(
                1, "Cat Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.CAT, CardSubtype.SOLDIER), Set.of(Keyword.VIGILANCE), true, false);
        CreateTokenEffect blockingCat = new CreateTokenEffect(
                "Cat Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.CAT, CardSubtype.SOLDIER), Set.of(Keyword.VIGILANCE), Set.of());
        addEffect(EffectSlot.ON_ATTACK, attackingCat);
        addEffect(EffectSlot.ON_BLOCK, new CreateTokenBlockingCombatOpponentEffect(blockingCat));
    }
}
