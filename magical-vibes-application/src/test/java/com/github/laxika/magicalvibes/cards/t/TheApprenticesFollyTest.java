package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheApprenticesFolly.class, GrizzlyBears.class, LlanowarElves.class})
class TheApprenticesFollyTest extends BaseCardTest {

    @Test
    void copiesEligibleCreaturesAndSacrificesReflectionsOnChapterThree() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, legendaryBears);
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new TheApprenticesFolly()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent firstReflection = findReflection(player1, "Grizzly Bears");
        assertThat(firstReflection.getCard().getSubtypes()).contains(CardSubtype.REFLECTION);
        assertThat(firstReflection.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(firstReflection.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);

        Permanent saga = findPermanent(player1, "The Apprentice's Folly");
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(elves.getId()).doesNotContain(bears.getId());
        harness.handlePermanentChosen(player1, elves.getId());
        harness.passBothPriorities();

        assertThat(findReflections(player1)).hasSize(2);

        saga = findPermanent(player1, "The Apprentice's Folly");
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findReflections(player1)).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("The Apprentice's Folly"));
    }

    private Permanent findReflection(Player player, String name) {
        return findReflections(player).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private List<Permanent> findReflections(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.REFLECTION))
                .toList();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
