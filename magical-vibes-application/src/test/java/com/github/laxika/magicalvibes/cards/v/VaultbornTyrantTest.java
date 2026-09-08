package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
}
