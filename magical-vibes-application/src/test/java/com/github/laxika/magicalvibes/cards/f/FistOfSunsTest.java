package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeaconOfTomorrows;
import com.github.laxika.magicalvibes.cards.t.Tyrranax;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FistOfSuns.class, Tyrranax.class, BeaconOfTomorrows.class})
class FistOfSunsTest extends BaseCardTest {

    @Test
    void controllerCanCastAHighManaValueCreatureForWubrg() {
        harness.addToBattlefield(player1, new FistOfSuns());
        harness.setHand(player1, List.of(new Tyrranax()));
        addWubrg(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertWubrgSpent();
    }

    @Test
    void alternativeCostRequiresAllFiveColors() {
        harness.addToBattlefield(player1, new FistOfSuns());
        harness.setHand(player1, List.of(new Tyrranax()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsDoNotUseControllerOwnedFistOfSuns() {
        harness.addToBattlefield(player1, new FistOfSuns());
        harness.setHand(player2, List.of(new Tyrranax()));
        addWubrg(player2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerCanStillCastNormallyWithoutWubrg() {
        harness.addToBattlefield(player1, new FistOfSuns());
        harness.setHand(player1, List.of(new Tyrranax()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void controllerCanCastNoncreatureSpellsForWubrg() {
        harness.addToBattlefield(player1, new FistOfSuns());
        harness.setHand(player1, List.of(new BeaconOfTomorrows()));
        addWubrg(player1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertWubrgSpent();
    }

    private void addWubrg(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private void assertWubrgSpent() {
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
