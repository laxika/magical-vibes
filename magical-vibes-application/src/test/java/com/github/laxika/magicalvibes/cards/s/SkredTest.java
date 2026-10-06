package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BroodingSaurian;
import com.github.laxika.magicalvibes.cards.a.AdarkarValkyrie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skred.class, SnowCoveredMountain.class, BroodingSaurian.class, AdarkarValkyrie.class})
class SkredTest extends BaseCardTest {

    @Test
    @DisplayName("Skred deals damage equal to the snow permanents its controller controls")
    void dealsDamageEqualToControlledSnowPermanents() {
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        harness.addToBattlefield(player1, new BroodingSaurian());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodingSaurian());

        castSkred(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Skred deals no damage when its controller controls no snow permanents")
    void dealsNoDamageWithoutSnowPermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodingSaurian());

        castSkred(target.getId());

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Skred counts only snow permanents controlled by its controller")
    void countsOnlyControllersSnowPermanents() {
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        addSnowPermanent(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodingSaurian());

        castSkred(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Skred counts snow permanents at resolution")
    void countsSnowPermanentsAtResolution() {
        Permanent firstSnowPermanent = addSnowPermanent(player1);
        addSnowPermanent(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodingSaurian());

        UUID targetId = target.getId();
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).remove(firstSnowPermanent);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Skred cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Snow-Covered Mountain");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skred counts snow creatures and tapped snow lands and can target its controller's creature")
    void countsSnowCreaturesAndTappedLands() {
        addSnowPermanent(player1).tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AdarkarValkyrie());

        castSkred(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Adarkar Valkyrie");
    }

    @Test
    @DisplayName("Skred destroys a creature when its damage is lethal")
    void lethalDamageDestroysCreature() {
        for (int i = 0; i < 4; i++) {
            addSnowPermanent(player1);
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodingSaurian());

        castSkred(target.getId());

        harness.assertNotOnBattlefield(player2, "Brooding Saurian");
        harness.assertInGraveyard(player2, "Brooding Saurian");
    }

    @Test
    @DisplayName("Skred cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSnowPermanent(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new SnowCoveredMountain());
    }

    private void castSkred(UUID targetId) {
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
