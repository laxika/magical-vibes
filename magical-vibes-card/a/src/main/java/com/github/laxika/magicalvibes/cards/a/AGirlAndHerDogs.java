package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithChosenNameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "279")
@CardRegistration(set = "MB2", collectorNumber = "515")
public class AGirlAndHerDogs extends Card {

    private static final CreateTokenEffect DOG_TOKEN = new CreateTokenEffect(
            CardType.CREATURE, 1, "Dog", 1, 1, CardColor.WHITE, null, List.of(CardSubtype.DOG),
            Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, true, 0, Set.of(),
            Set.of(CardSupertype.LEGENDARY));

    public AGirlAndHerDogs() {
        CreateTokenWithChosenNameEffect createNamedDog = new CreateTokenWithChosenNameEffect(DOG_TOKEN);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, createNamedDog);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, createNamedDog);

        PermanentCount legendaryCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(legendaryCreatures, legendaryCreatures));
    }
}
