package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueGreaterThanEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "189")
public class LaviniaAzoriusRenegade extends Card {

    public LaviniaAzoriusRenegade() {
        addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsWithManaValueGreaterThanEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.TARGET_PLAYER),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                null,
                List.of(new CounterSpellEffect()),
                new NotCondition(new SpellManaSpentAtLeast(1))));
    }
}
