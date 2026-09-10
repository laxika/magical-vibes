package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "8")
public class DecreeOfJustice extends Card {

    public DecreeOfJustice() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new XValue(), "Angel", 4, 4,
                CardColor.WHITE, List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of()));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}{W}",
                List.of(new PayXManaCreateXTokensEffect(CreateTokenEffect.whiteSoldier(1)),
                        new DrawCardEffect(1)),
                "Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.)"));
    }
}
