package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "220")
@CardRegistration(set = "THB", collectorNumber = "268")
public class KlothysGodOfDestiny extends Card {

    public KlothysGodOfDestiny() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.RED, ManaColor.GREEN), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
                        new CardTypePredicate(CardType.LAND),
                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN)),
                        SequenceEffect.of(
                                new GainLifeEffect(2),
                                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT))));
    }
}
