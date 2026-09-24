package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "665")
@CardRegistration(set = "MH1", collectorNumber = "207")
public class LavabellySliver extends Card {

    public LavabellySliver() {
        // Sliver creatures you control have "When this creature enters, it deals 1 damage to target
        // player or planeswalker and you gain 1 life."
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new SequenceEffect(List.of(
                        new EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect(1, PlayerRelation.ANY),
                        new GainLifeEffect(1))),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
