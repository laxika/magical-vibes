package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachAurasEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "5")
@CardRegistration(set = "WOC", collectorNumber = "41")
public class LiberatedLivestock extends Card {

    public LiberatedLivestock() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokensAndAttachAurasEffect(List.of(
                new CreateTokenEffect("Cat", 1, 1, CardColor.WHITE, List.of(CardSubtype.CAT),
                        Set.of(Keyword.LIFELINK), Set.of()),
                new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE, List.of(CardSubtype.BIRD),
                        Set.of(Keyword.FLYING), Set.of()),
                new CreateTokenEffect("Ox", 2, 4, CardColor.WHITE, List.of(CardSubtype.OX),
                        Set.of(), Set.of())
        )));
    }
}
