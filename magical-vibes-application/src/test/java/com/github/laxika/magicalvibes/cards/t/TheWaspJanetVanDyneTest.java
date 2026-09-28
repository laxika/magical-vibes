package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TheWaspJanetVanDyne.class, GrizzlyBears.class})
class TheWaspJanetVanDyneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to a tapped creature an opponent controls")
    void etbDealsFourDamageToTappedOpponentCreature() {
        Permanent target = addTappedCreature(player2);
        castWasp(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TheWaspJanetVanDyne()));
        addWaspMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped creature its controller controls")
    void cannotTargetOwnTappedCreature() {
        Permanent target = addTappedCreature(player1);
        harness.setHand(player1, List.of(new TheWaspJanetVanDyne()));
        addWaspMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("ETB does not damage the target if it becomes untapped before resolution")
    void etbFizzlesWhenTargetBecomesUntapped() {
        Permanent target = addTappedCreature(player2);
        castWasp(target.getId());

        harness.passBothPriorities();
        target.untap();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = addCreature(player);
        permanent.tap();
        return permanent;
    }

    private void castWasp(UUID targetId) {
        harness.setHand(player1, List.of(new TheWaspJanetVanDyne()));
        addWaspMana();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addWaspMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
