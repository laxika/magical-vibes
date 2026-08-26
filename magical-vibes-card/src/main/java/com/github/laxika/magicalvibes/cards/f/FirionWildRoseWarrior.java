package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "FIN", collectorNumber = "137")
public class FirionWildRoseWarrior extends Card {

    public FirionWildRoseWarrior() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE, GrantScope.ALL_OWN_CREATURES, new PermanentIsEquippedPredicate()));
        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                                true, Map.of(EffectSlot.STATIC, List.of(new ReduceEquipCostEffect(2))))));
    }
}
