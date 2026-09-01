package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FUT", collectorNumber = "155")
public class VirulentSliver extends Card {

    public VirulentSliver() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER),
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
