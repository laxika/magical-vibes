package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfSourceCardColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "103")
public class WrathOfSod extends Card {

    public WrathOfSod() {
        addSpellEffect(PutCountersEffect.onEach(CounterType.MANABOND, 1, new PermanentIsCreaturePredicate()));
        addSpellEffect(new GrantEffectsToCounterBearersEffect(CounterType.MANABOND, List.of(
                new SetCardTypesEffect(Set.of(CardType.LAND), Subjects.otherLands()),
                new LoseAllCreatureTypesEffect(Subjects.otherLands()),
                new LoseAllLandTypesEffect(Subjects.otherLands(), null),
                new LosesAllAbilitiesEffect(Subjects.otherLands()),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true, null, List.of(new AwardManaOfSourceCardColorsEffect()),
                                "{T}: Add one mana of this card's color."),
                        Subjects.otherLands()))));
    }
}
