package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardWithConditionalEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "220")
public class KlothysGodOfDestiny extends Card {

    public KlothysGodOfDestiny() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.RED, ManaColor.GREEN), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
                        new CardTypePredicate(CardType.LAND),
                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN)),
                        SequenceEffect.of(
                                new GainLifeEffect(2),
                                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT))));
    }
}
