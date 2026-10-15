package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DRC", collectorNumber = "16")
@CardRegistration(set = "DRC", collectorNumber = "32")
@CardRegistration(set = "FDC", collectorNumber = "243")
public class AdaptiveOmnitool extends Card {

    public AdaptiveOmnitool() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                artifactsYouControl, artifactsYouControl, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        6, new CardTypePredicate(CardType.ARTIFACT)));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
