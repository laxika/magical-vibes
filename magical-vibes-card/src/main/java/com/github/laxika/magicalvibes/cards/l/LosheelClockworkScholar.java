package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "38")
public class LosheelClockworkScholar extends Card {

    public LosheelClockworkScholar() {
        addEffect(EffectSlot.STATIC, new PreventCombatDamageToAttackingCreaturesYouControlEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));

        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.CREATURE),
                        new OncePerTurnTriggerEffect(new DrawCardEffect(1))));
    }
}
