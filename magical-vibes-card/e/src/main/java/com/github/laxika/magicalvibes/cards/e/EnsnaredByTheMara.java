package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "84")
@CardRegistration(set = "WHO", collectorNumber = "384")
@CardRegistration(set = "WHO", collectorNumber = "689")
@CardRegistration(set = "WHO", collectorNumber = "975")
public class EnsnaredByTheMara extends Card {

    public static final String CAST_OPTION = "You may cast the exiled card without paying its mana cost";
    public static final String DAMAGE_OPTION =
            "Exile the top four cards and take damage equal to their total mana value";

    public EnsnaredByTheMara() {
        // Each opponent faces it in turn order, the chosen option performed before the next one chooses; "they" and
        // "that player" are the opponent facing it.
        addSpellEffect(TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(Subjects.eachOpponent(), List.of(
                new ChooseOneEffect.ChooseOneOption(CAST_OPTION, SequenceEffect.of(
                        ExileCardsFromTopUntilEffect.untilNonland(Subjects.targetPlayerDeclaredElsewhere()),
                        new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(
                                StepKind.EXILE, ExileCardsFromTopUntilEffect.NONLAND)))),
                new ChooseOneEffect.ChooseOneOption(DAMAGE_OPTION, SequenceEffect.of(
                        ExileTopCardOfOwnLibraryEffect.of(Subjects.targetPlayerDeclaredElsewhere(), 4),
                        DealDamageEffect.to(Subjects.targetPlayerDeclaredElsewhere(),
                                AmountThisWay.exiledManaValue()))))));
    }
}
