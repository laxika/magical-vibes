package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "8")
@CardRegistration(set = "PIP", collectorNumber = "425")
@CardRegistration(set = "PIP", collectorNumber = "536")
@CardRegistration(set = "PIP", collectorNumber = "953")
public class PrestonGarveyMinuteman extends Card {

    public PrestonGarveyMinuteman() {
        target(TargetFilters.landYouControl(), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new CreateTokenAttachedToTargetEffect(settlementToken(), new PermanentIsLandPredicate()));

        addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED, new PermanentIsEnchantedPredicate()));
    }

    private static CreateTokenEffect settlementToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Settlement",
                0,
                0,
                CardColor.GREEN,
                Set.of(CardColor.GREEN),
                List.of(CardSubtype.AURA),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(), GrantScope.ENCHANTED_PERMANENT)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of())
                .withTokenTargetFilter(TargetFilters.land());
    }
}
