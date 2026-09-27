package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TriSentinelActOfVengeance.class, AirElemental.class, GrizzlyBears.class})
class TriSentinelActOfVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and deals 3 damage to one opposing creature")
    void entersAndDamagesOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castTriSentinel(List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows no ETB targets")
    void allowsNoTargets() {
        castTriSentinel(List.of());

        harness.assertOnBattlefield(player1, "Tri-Sentinel, Act of Vengeance");
    }

    @Test
    @DisplayName("Allows at most one ETB target per opponent")
    void allowsAtMostOneTargetPerOpponent() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unearth returns it to the battlefield")
    void unearthReturnsItToBattlefield() {
        harness.setGraveyard(player1, List.of(new TriSentinelActOfVengeance()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Tri-Sentinel, Act of Vengeance");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    private void castTriSentinel(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new TriSentinelActOfVengeance()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }
}
