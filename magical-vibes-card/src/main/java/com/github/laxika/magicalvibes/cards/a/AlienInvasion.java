package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "200")
public class AlienInvasion extends Card {

    public AlienInvasion() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new CreateXTokenWithXCountersEffect(
                        alienToken(), new CountersOnSource(CounterType.INVASION), CounterType.PLUS_ONE_PLUS_ONE),
                new PutCountersOnSourceCardEffect(CounterType.INVASION)));
    }

    private static CreateTokenEffect alienToken() {
        return new CreateTokenEffect(
                1,
                "Alien",
                1,
                1,
                CardColor.RED,
                List.of(CardSubtype.ALIEN),
                Set.of(Keyword.HASTE),
                Set.of(),
                Map.of(EffectSlot.STATIC, new MustAttackEffect()));
    }
}
