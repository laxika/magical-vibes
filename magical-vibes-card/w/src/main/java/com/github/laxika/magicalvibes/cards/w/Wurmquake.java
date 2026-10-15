package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "19")
@CardRegistration(set = "ONC", collectorNumber = "57")
public class Wurmquake extends Card {

    public Wurmquake() {
        addEffect(EffectSlot.SPELL, wurmToken(new Fixed(1)));
        addEffect(EffectSlot.SPELL, wurmToken(new OpponentsWithAtLeastPoisonCounters(3)));
        addCastingOption(new FlashbackCast("{8}{G}{G}"));
    }

    private CreateTokenEffect wurmToken(DynamicAmount amount) {
        return new CreateTokenEffect(
                CardType.CREATURE,
                amount,
                "Phyrexian Wurm",
                new ManaSpentToCast(),
                new ManaSpentToCast(),
                CardColor.GREEN,
                null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.WURM),
                Set.of(Keyword.TRAMPLE, Keyword.TOXIC),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
