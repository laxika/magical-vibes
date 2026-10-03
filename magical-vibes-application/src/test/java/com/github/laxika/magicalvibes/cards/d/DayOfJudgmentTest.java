package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.c.CrystalBall;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DayOfJudgment.class, RuneclawBear.class, LlanowarElves.class, Ornithopter.class, CrystalBall.class})
class DayOfJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys artifact creatures but leaves noncreature artifacts")
    void destroysArtifactCreaturesButLeavesNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new CrystalBall());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Crystal Ball");
        harness.assertNotInGraveyard(player1, "Crystal Ball");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }


    @Test
    @DisplayName("Destroys all creatures on both sides")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Day of Judgment");
    }

    @Test
    @DisplayName("Indestructible creature survives Day of Judgment")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());

        Permanent bears2 = findPermanent(player2, "Runeclaw Bear");
        bears2.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player 1's creature is destroyed
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        // Player 2's indestructible creature survives
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Creature with regeneration shield survives Day of Judgment")
    void creatureWithRegenerationShieldSurvives() {
        harness.addToBattlefield(player1, new RuneclawBear());

        Permanent bears = findPermanent(player1, "Runeclaw Bear");
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Creature survives via regeneration since cannotBeRegenerated is false
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Day of Judgment goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Day of Judgment");
    }
}
