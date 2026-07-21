package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FerventPaincasterTest extends BaseCardTest {

    // ===== {T}: deal 1 damage to target player or planeswalker =====

    @Test
    @DisplayName("First ability deals 1 damage to target player and does not exert")
    void firstAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent paincaster = addReadyPaincaster(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(paincaster.isTapped()).isTrue();
        assertThat(paincaster.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("First ability can't target a creature")
    void firstAbilityCannotTargetCreature() {
        addReadyPaincaster(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== {T}, Exert: deal 1 damage to target creature =====

    @Test
    @DisplayName("Second ability deals 1 damage to target creature, destroying a 1/1")
    void secondAbilityDealsDamageToCreature() {
        addReadyPaincaster(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, 1, null, elvesId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Exerting keeps the creature from untapping next untap step")
    void secondAbilityExertsSelf() {
        Permanent paincaster = addReadyPaincaster(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        assertThat(paincaster.isTapped()).isTrue();
        assertThat(paincaster.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can't target a player")
    void secondAbilityCannotTargetPlayer() {
        addReadyPaincaster(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPaincaster(Player player) {
        Permanent perm = new Permanent(new FerventPaincaster());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
