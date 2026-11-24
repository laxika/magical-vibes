package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "70")
@CardRegistration(set = "WHO", collectorNumber = "373")
@CardRegistration(set = "WHO", collectorNumber = "675")
@CardRegistration(set = "WHO", collectorNumber = "964")
public class ThisIsHowItEnds extends Card {

    public static final String LOSE_LIFE_OPTION = "They lose 5 life";
    public static final String SHUFFLE_CREATURE_OPTION = "They shuffle another creature they own into their library";

    public ThisIsHowItEnds() {
        // "They" is the owner who shuffled the target in, facing the choice; the target is gone by then, so every
        // creature they own is "another" one.
        PlayerSubject they = Subjects.targetPlayerDeclaredElsewhere();
        target(TargetFilters.creature())
                .addSpellEffect(new ShuffleTargetPermanentIntoLibraryEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(
                Subjects.playersThisWay(StepKind.SHUFFLE_INTO_LIBRARY), List.of(
                        new ChooseOneEffect.ChooseOneOption(LOSE_LIFE_OPTION, new LoseLifeEffect(5, they)),
                        new ChooseOneEffect.ChooseOneOption(SHUFFLE_CREATURE_OPTION, SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1,
                                                Subjects.ownedBy(they, new PermanentIsCreaturePredicate()),
                                                "another creature you own to shuffle into your library")
                                        .chosenBy(they),
                                ShuffleTargetPermanentIntoLibraryEffect.of(Subjects.thisWay(StepKind.CHOOSE)))))));
    }
}
