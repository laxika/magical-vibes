package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "8")
@CardRegistration(set = "VMA", collectorNumber = "22")
@CardRegistration(set = "DDO", collectorNumber = "7")
@CardRegistration(set = "A25", collectorNumber = "11")
@CardRegistration(set = "C14", collectorNumber = "70")
@CardRegistration(set = "C20", collectorNumber = "85")
public class DecreeOfJustice extends Card {

    public DecreeOfJustice() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new XValue(), "Angel", 4, 4,
                CardColor.WHITE, List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of()));

        addEffect(EffectSlot.ON_SELF_CYCLED, new PayXManaCreateXTokensEffect(CreateTokenEffect.whiteSoldier(1)));
        addCycling("{2}{W}");
    }
}
