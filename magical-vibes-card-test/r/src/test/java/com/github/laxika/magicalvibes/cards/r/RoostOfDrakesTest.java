package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.cards.s.ShellShield;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoostOfDrakes.class, AcademyDrake.class, ShellShield.class})
class RoostOfDrakesTest extends BaseCardTest {

    @Test
    @DisplayName("A kicked Roost of Drakes enters with a Drake token")
    void kickedRoostCreatesDrakeOnEntry() {
        harness.setHand(player1, List.of(new RoostOfDrakes()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(drakes()).hasSize(1);
        assertDrake(drakes().getFirst());
    }

    @Test
    @DisplayName("Casting a kicked spell creates a Drake token")
    void kickedSpellCreatesDrake() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(drakes()).hasSize(1);
        assertDrake(drakes().getFirst());
    }

    @Test
    @DisplayName("Casting a non-kicked spell does not create a Drake token")
    void nonKickedSpellDoesNotCreateDrake() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(drakes()).isEmpty();
    }

    @Test
    @DisplayName("An unkicked Roost enters without creating a Drake")
    void unkickedRoostDoesNotCreateDrakeOnEntry() {
        harness.setHand(player1, List.of(new RoostOfDrakes()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Roost of Drakes");
        assertThat(drakes()).isEmpty();
    }

    @Test
    @DisplayName("A Roost entering without being cast creates no Drake")
    void roostEnteringWithoutBeingCastDoesNotCreateDrake() {
        harness.enterBattlefieldAndReturn(player1, new RoostOfDrakes());
        resolveAllTriggers();

        assertThat(drakes()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's kicked spell does not trigger your Roost")
    void opponentsKickedSpellDoesNotCreateDrake() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RoostOfDrakes()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player2, 0);
        resolveAllTriggers();

        assertThat(drakes()).isEmpty();
        assertThat(countPermanents(player2, "Drake")).isEqualTo(1);
    }

    @Test
    @DisplayName("An existing Roost triggers before another kicked Roost enters")
    void existingRoostTriggersBeforeKickedRoostResolves() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new RoostOfDrakes()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(drakes()).hasSize(1);
        assertThat(countPermanents(player1, "Roost of Drakes")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(drakes()).hasSize(2);
        assertThat(countPermanents(player1, "Roost of Drakes")).isEqualTo(2);
    }

    @Test
    @DisplayName("A kicked instant creates one Drake for each Roost")
    void kickedInstantTriggersEachRoost() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.addToBattlefield(player1, new RoostOfDrakes());
        var creature = harness.addToBattlefieldAndReturn(player1, new AcademyDrake());
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(drakes()).hasSize(2);
        drakes().forEach(this::assertDrake);
    }

    private List<com.github.laxika.magicalvibes.model.Permanent> drakes() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DRAKE))
                .toList();
    }

    private void assertDrake(com.github.laxika.magicalvibes.model.Permanent drake) {
        assertThat(drake.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(drake.getCard().getPower()).isEqualTo(2);
        assertThat(drake.getCard().getToughness()).isEqualTo(2);
        assertThat(drake.getCard().getKeywords()).contains(Keyword.FLYING);
    }
}
