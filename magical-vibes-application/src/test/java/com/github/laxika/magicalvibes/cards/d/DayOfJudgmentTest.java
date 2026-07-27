package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DayOfJudgmentTest extends BaseCardTest {

    // ===== Resolution =====

    @Test
    @DisplayName("Destroys all creatures on both sides")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Day of Judgment");
    }

    @Test
    @DisplayName("Indestructible creature survives Day of Judgment")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears2 = findPermanent(player2, "Grizzly Bears");
        bears2.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player 1's creature is destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Player 2's indestructible creature survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature with regeneration shield survives Day of Judgment")
    void creatureWithRegenerationShieldSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Creature survives via regeneration since cannotBeRegenerated is false
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Day of Judgment goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Day of Judgment");
    }
}
