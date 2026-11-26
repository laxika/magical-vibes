package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathPowerThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "69")
@CardRegistration(set = "WHO", collectorNumber = "674")
public class GenesisOfTheDaleks extends Card {

    private static final CreateTokenEffect DALEKS = new CreateTokenEffect(
            new CountersOnSource(CounterType.LORE), "Dalek", 3, 3, CardColor.BLACK,
            List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT));

    public GenesisOfTheDaleks() {
        addTrigger(Triggers.chapter(1), DALEKS);
        addTrigger(Triggers.chapter(2), DALEKS);
        addTrigger(Triggers.chapter(3), DALEKS);

        // The life loss is a step after the destruction, so it counts the Daleks that died to it.
        addTrigger(Triggers.chapter(4), TargetPlayerChoosesOneEffect.forTargetedPlayerVillainousChoice(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all Dalek creatures and each of your opponents loses life equal to the total power"
                                + " of Daleks that died this turn",
                        List.of(DestroyTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.DALEK)))),
                                new LoseLifeEffect(new CreatureSubtypeDeathPowerThisTurn(
                                        CardSubtype.DALEK, CountScope.ANY_PLAYER), Subjects.eachOpponent()))),
                new ChooseOneEffect.ChooseOneOption("Destroy all non-Dalek creatures",
                        DestroyTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DALEK)))))))));
        setSagaChapterTargetFilter(4, Set.of(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")));
    }
}
