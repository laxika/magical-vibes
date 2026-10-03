package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlphaDeathclaw.class, GrizzlyBears.class, Forest.class})
class AlphaDeathclawTest extends BaseCardTest {

    @Test
    @DisplayName("When Alpha Deathclaw enters, it destroys the chosen permanent")
    void entersAndDestroysTargetPermanent() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AlphaDeathclaw(), "{4}{B}{G}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("When Alpha Deathclaw becomes monstrous, it destroys the chosen permanent")
    void becomingMonstrousDestroysTargetPermanent() {
        Permanent deathclaw = harness.addToBattlefieldAndReturn(player1, new AlphaDeathclaw());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(deathclaw.isMonstrous()).isTrue();
        assertThat(deathclaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Alpha Deathclaw's triggers cannot target a player")
    void triggerCannotTargetPlayer() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AlphaDeathclaw(), "{4}{B}{G}");
        harness.passBothPriorities();

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, playerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enters trigger can destroy a land controlled by Alpha Deathclaw's controller")
    void entersAndDestroysOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new AlphaDeathclaw(), "{4}{B}{G}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Alpha Deathclaw");
    }

    @Test
    @DisplayName("The mandatory enters trigger can destroy Alpha Deathclaw itself")
    void entersAndDestroysItself() {
        harness.castFromHand(player1, new AlphaDeathclaw(), "{4}{B}{G}");
        harness.passBothPriorities();

        Permanent deathclaw = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, deathclaw.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpha Deathclaw");
        harness.assertInGraveyard(player1, "Alpha Deathclaw");
    }

    @Test
    @DisplayName("A monstrous Alpha Deathclaw can activate monstrosity again, with no further effect")
    void canActivateMonstrosityAfterBecomingMonstrous() {
        Permanent deathclaw = harness.addToBattlefieldAndReturn(player1, new AlphaDeathclaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deathclaw.isMonstrous()).isTrue();
        assertThat(deathclaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Alpha Deathclaw");
    }

    @Test
    @DisplayName("Two pending monstrosity activations add counters and trigger destruction only once")
    void overlappingMonstrosityActivationsOnlyApplyOnce() {
        Permanent deathclaw = harness.addToBattlefieldAndReturn(player1, new AlphaDeathclaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(deathclaw.isMonstrous()).isTrue();
        assertThat(deathclaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Alpha Deathclaw");
        harness.assertInGraveyard(player2, "Forest");
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
