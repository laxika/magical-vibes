package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoldeviMachinist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusGuildmage.class, GrizzlyBears.class, RodOfRuin.class, Shock.class, SoldeviMachinist.class})
class AzoriusGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps target creature")
    void tapsTargetCreature() {
        addGuildmage(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaForWhiteAbility(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability counters an activated ability")
    void countersActivatedAbility() {
        addGuildmage(player1);
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        addManaForBlueAbility(player1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, 1, null, rod.getCard().getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability cannot target a spell")
    void cannotTargetSpell() {
        addGuildmage(player1);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        addManaForBlueAbility(player1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a mana ability")
    void cannotTargetManaAbility() {
        addGuildmage(player1);
        Permanent machinist = harness.addToBattlefieldAndReturn(player2, new SoldeviMachinist());
        machinist.setSummoningSick(false);
        addManaForBlueAbility(player1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, machinist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addGuildmage(Player player) {
        harness.addToBattlefield(player, new AzoriusGuildmage());
    }

    private void addManaForWhiteAbility(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private void addManaForBlueAbility(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
