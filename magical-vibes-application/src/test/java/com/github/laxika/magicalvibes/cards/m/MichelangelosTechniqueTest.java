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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        Card llanowarElves = new LlanowarElves();
        Card serraAngel = new SerraAngel();
        setLibrary(llanowarElves, serraAngel, new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(new MichelangelosTechnique()));
        addMana(4, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(llanowarElves.getId(), serraAngel.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value limit of 6");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(serraAngel.getId()));
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and enters tapped and attacking")
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

    private void addMana(int colorless, int green) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, green);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
