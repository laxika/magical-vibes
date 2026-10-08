package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartlessSummoning;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VaultbornTyrant.class, GrizzlyBears.class, LeatherbackBaloth.class})
class VaultbornTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gains 3 life and draws a card")
    void ownEntryGainsLifeAndDraws() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new VaultbornTyrant());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature with power 4 or greater entering under its controller's control gains 3 life and draws a card")
    void anotherHighPowerCreatureEntryGainsLifeAndDraws() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VaultbornTyrant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature with power less than 4 does not trigger the entry ability")
    void lowPowerCreatureEntryDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VaultbornTyrant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed(HeartlessSummoning.class)
    @DisplayName("Its own entry triggers even when continuous effects reduce its power below four")
    void ownEntryTriggersWithPowerBelowFour() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new HeartlessSummoning());

        harness.enterBattlefieldAndReturn(player1, new VaultbornTyrant());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's high-power creature does not trigger the entry ability")
    void opponentsCreatureDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new VaultbornTyrant());

        harness.enterBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lowering the entering creature's power after triggering does not stop the ability")
    void powerDecreaseAfterEntryDoesNotStopTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new VaultbornTyrant());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        entering.setPowerModifier(-2);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The entry ability still resolves after the entering creature dies")
    void enteringCreatureDyingDoesNotStopTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new VaultbornTyrant());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        entering.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leatherback Baloth");
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(HeartlessSummoning.class)
    @DisplayName("Continuous power reduction prevents another creature from qualifying at entry")
    void anotherCreatureMustHaveFourPowerAfterContinuousEffects() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new VaultbornTyrant());
        harness.addToBattlefield(player1, new HeartlessSummoning());

        harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("When it dies, it creates an artifact token copy whose entry ability triggers")
    void deathCreatesArtifactTokenCopyAndTriggersItsEntryAbility() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent tyrant = harness.enterBattlefieldAndReturn(player1, new VaultbornTyrant());
        harness.passBothPriorities();

        tyrant.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Vaultborn Tyrant");
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
                });
        harness.assertLife(player1, 26);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A token copy does not create another token copy when it dies")
    void tokenCopyDoesNotCopyItselfOnDeath() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent tyrant = harness.enterBattlefieldAndReturn(player1, new VaultbornTyrant());
        harness.passBothPriorities();

        tyrant.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        token.setMarkedDamage(6);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The artifact token copy retains the ability to trigger for other creatures")
    void artifactTokenTriggersForAnotherCreature() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new VaultbornTyrant());

        tyrant.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue());
    }
}
