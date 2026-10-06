package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepelIntruders.class, LlanowarElves.class, GrizzlyBears.class, MightOfOaks.class})
class RepelIntrudersTest extends BaseCardTest {

    @Test
    @DisplayName("Only {W} spent: creates two Kithkin Soldiers, does not counter the creature spell")
    void whiteOnlyMakesTokensDoesNotCounter() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RepelIntruders()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        // Two Kithkin Soldier tokens under the caster's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .hasSize(2);
        // Creature spell was not countered
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Only {U} spent: counters the creature spell, makes no tokens")
    void blueOnlyCountersMakesNoTokens() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RepelIntruders()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Llanowar Elves");
        // No tokens created
        harness.assertNotOnBattlefield(player2, "Kithkin Soldier");
    }

    @Test
    @DisplayName("{W}{U} spent: creates two Kithkin Soldiers and counters the creature spell")
    void bothColorsMakesTokensAndCounters() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RepelIntruders()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new RepelIntruders()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("White payment with no target creates the two specified tokens")
    void whiteWithoutTargetCreatesTokens() {
        harness.setHand(player1, List.of(new RepelIntruders()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getName()).isEqualTo("Kithkin Soldier");
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColors())
                            .containsExactly(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(
                            CardSubtype.KITHKIN,
                            CardSubtype.SOLDIER);
                });
        harness.assertInGraveyard(player1, "Repel Intruders");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blue payment with no target resolves without creating tokens")
    void blueWithoutTargetDoesNothing() {
        harness.setHand(player1, List.of(new RepelIntruders()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Kithkin Soldier");
        harness.assertInGraveyard(player1, "Repel Intruders");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Both colors may be spent while declining an available creature target")
    void bothColorsWithoutTargetLeavesCreatureSpellAlone() {
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new RepelIntruders()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .hasSize(2);
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("An illegal creature target prevents tokens even when only white was spent")
    void removedTargetPreventsWhiteTokenCreation() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new RepelIntruders()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new RepelIntruders()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, elves.getId());
        harness.assertInGraveyard(player1, "Llanowar Elves");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kithkin Soldier");
        harness.assertInGraveyard(player2, "Repel Intruders");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
