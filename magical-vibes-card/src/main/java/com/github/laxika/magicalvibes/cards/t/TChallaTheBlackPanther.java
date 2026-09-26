package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "7")
public class TChallaTheBlackPanther extends Card {

    private static final CreateTokenEffect VIBRANIUM_TOKEN = new CreateTokenEffect(
            CardType.ARTIFACT,
            1,
            "Vibranium",
            0,
            0,
            null,
            null,
            List.of(),
            Set.of(Keyword.INDESTRUCTIBLE),
            Set.of(),
            false,
            true,
            Map.of(),
            List.of(new ActivatedAbility(
                    true,
                    null,
                    List.of(new AwardRestrictedManaEffect(
                            ManaColor.COLORLESS, 1, new ManaRestriction.Powerstone())),
                    "{T}: Add {C}. This mana can't be spent to cast a nonartifact spell."
            )),
            false,
            false,
            false,
            0,
            Set.of()
    );

    public TChallaTheBlackPanther() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, VIBRANIUM_TOKEN);
        addEffect(EffectSlot.ON_ATTACK, VIBRANIUM_TOKEN);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardMinManaValuePredicate(4, true)
                )),
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))));
    }
}
