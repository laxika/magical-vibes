package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "3")
@CardRegistration(set = "SLD", collectorNumber = "1218")
@CardRegistration(set = "UMA", collectorNumber = "3")
@CardRegistration(set = "SOC", collectorNumber = "131")
@CardRegistration(set = "M3C", collectorNumber = "155")
public class EldraziConscription extends Card {

    public EldraziConscription() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(
                        10, 10, Set.of(Keyword.TRAMPLE), GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new SacrificePermanentsEffect(
                                2, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
