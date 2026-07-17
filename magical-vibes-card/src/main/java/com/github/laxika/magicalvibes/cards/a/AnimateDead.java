package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "140")
public class AnimateDead extends Card {

    public AnimateDead() {
        // "Enchant creature card in a graveyard." A reanimation Aura: it targets a creature card in
        // a graveyard at cast time. The graveyard-targeting SPELL effect drives cast-time targeting
        // (and marks the stack entry as a GRAVEYARD target); StackResolutionService reanimates the
        // enchanted creature under this Aura's controller and attaches this Aura to it.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
        // "Enchanted creature gets -1/-0."
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.ENCHANTED_CREATURE));
        // "When this Aura leaves the battlefield, that creature's controller sacrifices it."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SacrificeEnchantedCreatureOnLeaveEffect());
    }
}
