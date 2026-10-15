package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaffCapashenShipsMage.class, LeoninScimitar.class, ReyaDawnbringer.class,
        GrizzlyBears.class, TheFlameOfKeld.class})
class RaffCapashenShipsMageTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast artifact spell during opponent's turn with Raff on battlefield")
    void canCastArtifactDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Player1 can cast artifact with flash timing
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leonin Scimitar");
    }

    @Test
    @DisplayName("Can cast legendary creature during combat with Raff on battlefield")
    void canCastLegendaryCreatureDuringCombat() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    @Test
    @DisplayName("Cannot cast non-historic creature at instant speed with Raff on battlefield")
    void cannotCastNonHistoricCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spells lose flash timing when Raff leaves the battlefield")
    void artifactLosesFlashWhenRaffLeaves() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        // Remove Raff from battlefield
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Raff only grants flash to its controller's historic spells")
    void onlyAffectsController() {
        // Player2 controls Raff, player1 should not benefit
        harness.addToBattlefield(player2, new RaffCapashenShipsMage());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spells cannot be cast at instant speed without Raff")
    void cannotCastArtifactAtInstantSpeedWithoutRaff() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
    @Test
    @DisplayName("Can cast a nonlegendary Saga during the opponent's turn")
    void canCastSagaDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        TheFlameOfKeld saga = new TheFlameOfKeld();
        harness.setHand(player1, List.of(saga));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(saga);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "The Flame of Keld");
    }

    @Test
    @DisplayName("Raff can be cast during the opponent's turn without a flash grant")
    void raffHasItsOwnFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        RaffCapashenShipsMage raff = new RaffCapashenShipsMage();
        harness.setHand(player1, List.of(raff));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(raff);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Raff Capashen, Ship's Mage");
    }

    @Test
    @DisplayName("Historic spells can be cast in response to another spell")
    void canCastHistoricSpellWithNonemptyStack() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());
        harness.setHand(player1, List.of(new LeoninScimitar(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof LeoninScimitar)
                .hasSize(2);
    }
}
