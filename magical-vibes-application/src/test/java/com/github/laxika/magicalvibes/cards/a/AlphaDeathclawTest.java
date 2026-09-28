package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlphaDeathclaw.class, GrizzlyBears.class})
class AlphaDeathclawTest extends BaseCardTest {

    @Test
    @DisplayName("When Alpha Deathclaw enters, it destroys the chosen permanent")
    void entersAndDestroysTargetPermanent() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlphaDeathclaw()));
        addCastingMana();

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new AlphaDeathclaw()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, playerId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
