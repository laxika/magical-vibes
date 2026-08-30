package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "230")
public class HazezonTamar extends Card {

    public HazezonTamar() {
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        CreateTokenEffect sandWarrior = new CreateTokenEffect(
                CardType.CREATURE, landsYouControl, "Sand Warrior", 1, 1, CardColor.RED,
                Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.SAND, CardSubtype.WARRIOR), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterDelayedCreateTokenAtNextUpkeepEffect(sandWarrior));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ExileAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SAND),
                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)))));
    }
}
