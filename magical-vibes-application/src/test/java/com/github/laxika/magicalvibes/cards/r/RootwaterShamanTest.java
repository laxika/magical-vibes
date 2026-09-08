package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CursedLand;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wanderlust;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterShaman.class, GrizzlyBears.class, Wanderlust.class})
class RootwaterShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Enchant creature Aura can be cast at instant speed with Rootwater Shaman out")
    void enchantCreatureAuraGetsFlash() {
        harness.addToBattlefield(player1, new RootwaterShaman());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, host.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Wanderlust.class);
    }

    @Test
    @DisplayName("Enchant creature Aura can be cast during the opponent's turn")
    void enchantCreatureAuraCastableOnOpponentsTurn() {
        harness.addToBattlefield(player1, new RootwaterShaman());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castEnchantment(player1, 0, host.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @CardUsed({CursedLand.class, Forest.class})
    @DisplayName("Aura with enchant land does not gain flash")
    void enchantLandAuraDoesNotGetFlash() {
        harness.addToBattlefield(player1, new RootwaterShaman());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CursedLand()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enchant creature Aura has no flash without Rootwater Shaman")
    void noFlashWithoutShaman() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rootwater Shaman only grants flash to its controller")
    void onlyAffectsController() {
        harness.addToBattlefield(player2, new RootwaterShaman());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
