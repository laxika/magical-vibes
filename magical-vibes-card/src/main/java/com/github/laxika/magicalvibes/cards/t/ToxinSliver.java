package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "84")
public class ToxinSliver extends Card {

    public ToxinSliver() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new DestroyTargetPermanentEffect(true),
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
