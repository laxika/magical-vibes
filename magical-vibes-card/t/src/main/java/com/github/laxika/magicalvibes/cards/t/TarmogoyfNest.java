package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "68")
@CardRegistration(set = "M3C", collectorNumber = "120")
public class TarmogoyfNest extends Card {

    public TarmogoyfNest() {
        CardTypesAmongCardsInGraveyard cardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.ANY_PLAYER);
        CreateTokenEffect tarmogoyfToken = new CreateTokenEffect(
                1,
                "Tarmogoyf",
                0,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.LHURGOYF),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                        cardTypes, new Sum(cardTypes, new Fixed(1)))));

        // Enchanted land has "{1}{G}, {T}: Create a Tarmogoyf token."
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}{G}",
                        List.of(tarmogoyfToken),
                        "{1}{G}, {T}: Create a Tarmogoyf token."
                ),
                GrantScope.ENCHANTED_PERMANENT
        ));
    }
}
