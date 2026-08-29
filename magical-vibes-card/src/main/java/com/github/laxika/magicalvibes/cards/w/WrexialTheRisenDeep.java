package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;

@CardRegistration(set = "WWK", collectorNumber = "120")
public class WrexialTheRisenDeep extends Card {

    public WrexialTheRisenDeep() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD, true, true));
    }
}
