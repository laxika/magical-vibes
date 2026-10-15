package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeathsCaress;
import com.github.laxika.magicalvibes.cards.f.FiresOfUndeath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SightlessGhoul.class, FiresOfUndeath.class, DeathsCaress.class})
class SightlessGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sightless Ghoul cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new SightlessGhoul());

        Permanent attacker = addCreatureReady(player1, new SightlessGhoul());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Undying returns Sightless Ghoul with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new SightlessGhoul());
        harness.setHand(player2, List.of(new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, ghoul.getId());
        resolveAllTriggers();

        Permanent returnedGhoul = findPermanent(player1, "Sightless Ghoul");
        assertThat(returnedGhoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Sightless Ghoul");
    }

    @Test
    @DisplayName("Undying does not return Sightless Ghoul when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new SightlessGhoul());
        ghoul.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new DeathsCaress()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, ghoul.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sightless Ghoul");
        harness.assertInGraveyard(player1, "Sightless Ghoul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying returns a stolen Sightless Ghoul under its owner's control")
    void undyingReturnsToOwner() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new SightlessGhoul());
        gd.stolenCreatures.put(ghoul.getId(), player1.getId());
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, ghoul.getId());
        resolveAllTriggers();

        Permanent returnedGhoul = findPermanent(player1, "Sightless Ghoul");
        assertThat(returnedGhoul).isNotNull();
        assertThat(returnedGhoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Sightless Ghoul");
        harness.assertNotInGraveyard(player1, "Sightless Ghoul");
        harness.assertNotInGraveyard(player2, "Sightless Ghoul");
    }

    @Test
    @DisplayName("Sightless Ghoul can attack despite its blocking restriction")
    void canAttack() {
        Permanent ghoul = addCreatureReady(player1, new SightlessGhoul());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(ghoul.isAttacking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 18);
    }
}
