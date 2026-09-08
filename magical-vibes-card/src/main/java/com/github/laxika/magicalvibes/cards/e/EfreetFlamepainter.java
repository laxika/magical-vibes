package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;

@CardRegistration(set = "STX", collectorNumber = "98")
public class EfreetFlamepainter extends Card {

    public EfreetFlamepainter() {
        // Whenever this creature deals combat damage to a player, you may cast target instant or
        // sorcery card from your graveyard without paying its mana cost. If that spell would be
        // put into your graveyard, exile it instead.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true));
    }
}
