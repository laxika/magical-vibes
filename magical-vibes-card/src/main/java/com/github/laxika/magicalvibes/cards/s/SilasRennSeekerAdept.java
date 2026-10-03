package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "C16", collectorNumber = "43")
@CardRegistration(set = "BRC", collectorNumber = "129")
public class SilasRennSeekerAdept extends Card {

    public SilasRennSeekerAdept() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GrantTargetGraveyardCardCastEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false));
    }
}
