package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "57")
public class FatalPush extends Card {

    public FatalPush() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new ConditionalEffect(
                        new AllOf(List.of(
                                new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                                new TargetPermanentMatches(new PermanentMaxManaValuePredicate(4))
                        )),
                        new DestroyTargetPermanentEffect()),
                new ConditionalEffect(
                        new AllOf(List.of(
                                new NotCondition(new PermanentLeftBattlefieldUnderYourControlThisTurn()),
                                new TargetPermanentMatches(new PermanentMaxManaValuePredicate(2))
                        )),
                        new DestroyTargetPermanentEffect())
        ));
    }
}
