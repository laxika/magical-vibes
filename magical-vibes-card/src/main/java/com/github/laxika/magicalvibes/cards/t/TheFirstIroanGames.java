package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "170")
public class TheFirstIroanGames extends Card {

    public TheFirstIroanGames() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ConditionalEffect(
                new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                new DrawCardEffect(2)));

        addEffect(EffectSlot.SAGA_CHAPTER_IV, CreateTokenEffect.ofArtifactToken(
                1, "Gold", List.of(), List.of(new ActivatedAbility(
                        false, null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "Sacrifice this token: Add one mana of any color."))));
    }
}
