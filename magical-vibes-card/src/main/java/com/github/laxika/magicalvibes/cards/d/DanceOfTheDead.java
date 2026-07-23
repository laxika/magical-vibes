package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.UntapEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ICE", collectorNumber = "118")
public class DanceOfTheDead extends Card {

    public DanceOfTheDead() {
        // "Enchant creature card in a graveyard." Reanimation Aura: targets a creature card in a
        // graveyard at cast. StackResolutionService reanimates it (tapped) under this Aura's
        // controller and attaches this Aura to it.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .enterTapped(true)
                .build());
        // "Enchanted creature gets +1/+1 and doesn't untap during its controller's untap step."
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
        // "When this Aura leaves the battlefield, that creature's controller sacrifices it."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SacrificeEnchantedCreatureOnLeaveEffect());
        // "At the beginning of the upkeep of enchanted creature's controller, that player may
        // pay {1}{B}. If the player does, untap that creature."
        addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new MayPayManaEffect("{1}{B}", new UntapEquippedCreatureEffect(),
                        "pay {1}{B} to untap the creature", true));
    }
}
