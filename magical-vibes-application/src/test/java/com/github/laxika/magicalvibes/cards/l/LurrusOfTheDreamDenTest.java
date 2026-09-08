package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LurrusOfTheDreamDen.class, GrizzlyBears.class, DarksteelRelic.class,
        Forest.class, CrawWurm.class, LightningBolt.class})
class LurrusOfTheDreamDenTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a permanent card with mana value 2 or less from the graveyard")
    void castsEligiblePermanent() {
        harness.addToBattlefield(player1, new LurrusOfTheDreamDen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casts a noncreature permanent card from the graveyard")
    void castsEligibleArtifact() {
        harness.addToBattlefield(player1, new LurrusOfTheDreamDen());
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.setHand(player1, List.of());

        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Does not cast lands, nonpermanent cards, or permanents with mana value greater than 2")
    void rejectsIneligibleCards() {
        harness.addToBattlefield(player1, new LurrusOfTheDreamDen());
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.setGraveyard(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player1, List.of(new LightningBolt()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player1, List.of(new CrawWurm()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows only one graveyard permanent spell during each of your turns")
    void allowsOnlyOneSpellPerTurn() {
        harness.addToBattlefield(player1, new LurrusOfTheDreamDen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The permission is available only during the controller's turn")
    void onlyWorksDuringControllerTurn() {
        harness.addToBattlefield(player1, new LurrusOfTheDreamDen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
