package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MIC", collectorNumber = "15")
@CardRegistration(set = "MIC", collectorNumber = "53")
public class HordewingSkaab extends Card {

    public HordewingSkaab() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        new MayEffect(
                                SequenceEffect.of(
                                        new DrawCardEffect(1),
                                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                                "Draw a card and discard a card?"),
                        false,
                        true));
    }
}
