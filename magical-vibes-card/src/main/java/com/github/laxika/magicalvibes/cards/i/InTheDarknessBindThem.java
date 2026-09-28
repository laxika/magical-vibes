package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "58")
public class InTheDarknessBindThem extends Card {

    public InTheDarknessBindThem() {
        var wraith = new CreateTokenEffect(1, "Wraith", 3, 3, CardColor.BLACK,
                List.of(CardSubtype.WRAITH), Set.of(Keyword.MENACE), Set.of());
        addEffect(EffectSlot.SAGA_CHAPTER_I, wraith);
        addEffect(EffectSlot.SAGA_CHAPTER_I, new RingTemptsYouEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_II, wraith);
        addEffect(EffectSlot.SAGA_CHAPTER_II, new RingTemptsYouEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, wraith);
        addEffect(EffectSlot.SAGA_CHAPTER_III, new RingTemptsYouEffect());

        var target = TargetFilters.creatureAnOpponentControls();
        target(target, 0, 99)
                .addEffect(EffectSlot.SAGA_CHAPTER_IV,
                        GainControlOfTargetEffect.withTargetPredicate(
                                ControlDuration.END_OF_TURN, target.predicate()))
                .addEffect(EffectSlot.SAGA_CHAPTER_IV,
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addEffect(EffectSlot.SAGA_CHAPTER_IV,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addEffect(EffectSlot.SAGA_CHAPTER_IV, new RingTemptsYouEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_IV, Set.of(target));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
    }
}
