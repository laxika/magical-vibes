package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SOI", collectorNumber = "263")
public class SkeletonKey extends Card {

    public SkeletonKey() {
        // Equipped creature has skulk.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SKULK, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature deals combat damage to a player, you may draw a card. If you
        // do, discard a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        "Draw a card?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
