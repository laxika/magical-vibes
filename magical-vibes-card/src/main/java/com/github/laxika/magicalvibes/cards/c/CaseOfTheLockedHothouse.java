package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "155")
public class CaseOfTheLockedHothouse extends Card {

    public CaseOfTheLockedHothouse() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(7, new PermanentIsLandPredicate()),
                                new NotCondition(new SourceIsSolved())
                        )),
                        new SolveSourceEffect()));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(), new LookAtTopCardOfOwnLibraryEffect()));
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(), new PlayLandsFromTopOfLibraryEffect()));
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(), new AllowCastFromTopOfLibraryEffect(
                        Set.of(CardType.CREATURE, CardType.ENCHANTMENT))));
    }
}
