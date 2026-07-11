package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiltLeafArchdruidTest extends BaseCardTest {

    // ===== Trigger: Whenever you cast a Druid spell, you may draw a card =====

    private void setUpControllerMain() {
        harness.addToBattlefield(player1, new GiltLeafArchdruid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting a Druid spell offers a draw; accepting draws a card")
    void castingDruidSpellAcceptDraws() {
        setUpControllerMain();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GiltLeafArchdruid()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void castingDruidSpellDeclineNoDraw() {
        setUpControllerMain();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GiltLeafArchdruid()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Casting a non-Druid spell does not trigger")
    void castingNonDruidSpellDoesNotTrigger() {
        setUpControllerMain();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Activated ability: Tap seven Druids: gain control of all lands target player controls =====

    @Test
    @DisplayName("Tapping seven Druids gains control of all lands the target player controls")
    void gainControlOfAllLands() {
        // Source Druid at index 0, plus six more Druids = seven untapped Druids (auto-tapped as cost).
        Permanent source = new Permanent(new GiltLeafArchdruid());
        source.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(source);
        for (int i = 0; i < 6; i++) {
            addCreatureReady(player1, new GiltLeafArchdruid());
        }

        Permanent forestA = new Permanent(new Forest());
        Permanent forestB = new Permanent(new Forest());
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(forestA);
        gd.playerBattlefields.get(player2.getId()).add(forestB);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIdx, 0, player2.getId());
        harness.passBothPriorities();

        // Both lands moved to player1; the creature stayed with player2.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forestA.getId()))
                .anyMatch(p -> p.getId().equals(forestB.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(forestA.getId()))
                .noneMatch(p -> p.getId().equals(forestB.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot activate the ability with fewer than seven untapped Druids")
    void cannotActivateWithoutSevenDruids() {
        Permanent source = new Permanent(new GiltLeafArchdruid());
        source.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(source);
        // Only six Druids total (source + five).
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new GiltLeafArchdruid());
        }

        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> harness.activateAbility(player1, sourceIdx, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
    }
}
