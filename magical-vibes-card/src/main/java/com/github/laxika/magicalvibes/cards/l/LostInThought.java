package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceAuraEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "45")
public class LostInThought extends Card {

    public LostInThought() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());
        addActivatedAbility(new ActivatedAbility(false, null,
                        List.of(new ExileNCardsFromGraveyardCost(3, null),
                                new IgnoreSourceAuraEffectsUntilEndOfTurnEffect()),
                        "Exile three cards from your graveyard: Ignore this effect until end of turn.")
                .withActivatableByAnyPlayer()
                .withActivatableOnlyByEnchantedPermanentController());
    }
}
