package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MichelangelosTechnique.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, SerraAngel.class})
class MichelangelosTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to two creature cards with total mana value six or less onto the battlefield")
    void putsUpToTwoCreaturesWithinTotalManaValue() {
        Card llanowarElves = new LlanowarElves();
        Card serraAngel = new SerraAngel();
        Card grizzlyBears = new GrizzlyBears();
        setLibrary(llanowarElves, serraAngel, grizzlyBears,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                llanowarElves.getId(), serraAngel.getId(), grizzlyBears.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.totalManaValueBound()).isEqualTo(6);

        harness.handleMultipleCardsChosen(player1, List.of(llanowarElves.getId(), serraAngel.getId()));

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6)
                .contains(grizzlyBears)
                .doesNotContain(llanowarElves, serraAngel);
    }

    @Test
    @DisplayName("Rejects a selection whose combined mana value is over six")
    void rejectsSelectionOverTotalManaValue() {
        Card grizzlyBears = new GrizzlyBears();
        Card serraAngel = new SerraAngel();
        setLibrary(grizzlyBears, serraAngel, new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(grizzlyBears.getId(), serraAngel.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value limit of 6");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(serraAngel.getId()));
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker to pay the alternate cost")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        setLibrary(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(3, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void mayChooseNoCreaturesFromAShortLibrary() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        setLibrary(bears, forest);
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, forest);
        harness.assertInGraveyard(player1, "Michelangelo's Technique");
    }

    @Test
    void onlyLooksAtTopEightAndLeavesUntouchedCardsAboveTheRemainder() {
        Card bears = new GrizzlyBears();
        List<Card> lookedAt = List.of(bears, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        Card ninth = new SerraAngel();
        Card tenth = new LlanowarElves();
        List<Card> library = new ArrayList<>(lookedAt);
        library.addAll(List.of(ninth, tenth));
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ninth.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(9).startsWith(ninth, tenth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 9))
                .containsExactlyInAnyOrderElementsOf(lookedAt.subList(1, 8));
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isAttacking()).isFalse();
    }

    @Test
    void rejectsMoreThanTwoCreaturesEvenWithinManaValueLimit() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        Card third = new LlanowarElves();
        setLibrary(first, second, third);
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        setLibrary();
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Michelangelo's Technique");
    }

    @Test
    void creaturesFoundByASneakedSorceryEnterUntappedAndNotAttacking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card elves = new LlanowarElves();
        setLibrary(elves);
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(3, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(elves.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isAttacking()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Michelangelo's Technique");
    }

    private void addMana(int colorless, int green) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, green);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
