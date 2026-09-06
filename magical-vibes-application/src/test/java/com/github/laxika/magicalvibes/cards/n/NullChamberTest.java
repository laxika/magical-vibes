package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BadRiver;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullChamber.class, NobleElephant.class, Disenchant.class, BadRiver.class, Replenish.class})
class NullChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Null Chamber asks the controller, then the opponent, for a card name")
    void bothPlayersNameACard() {
        harness.setHand(player1, List.of(new NullChamber(), new NobleElephant()));
        harness.setHand(player2, List.of(new BadRiver()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Null Chamber");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleListChoice(player1, "Bad River");

        harness.assertNotOnBattlefield(player1, "Null Chamber");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Noble Elephant");

        Permanent perm = findPermanent(player1, "Null Chamber");
        assertThat(perm.getChosenName()).isEqualTo("Bad River");
        assertThat(perm.getSecondChosenName()).isEqualTo("Noble Elephant");
    }

    @Test
    @DisplayName("Basic land names are not offered as choices")
    void basicLandNamesAreNotOffered() {
        harness.setHand(player1, List.of(new NullChamber(), new NobleElephant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        List<String> options = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options();
        assertThat(options).contains("Noble Elephant");
        assertThat(options).doesNotContain("Plains", "Island", "Swamp", "Mountain", "Forest");
    }

    @Test
    @DisplayName("Neither player can cast a spell with either chosen name")
    void neitherPlayerCanCastEitherChosenName() {
        addReadyNullChamber(player1, "Noble Elephant", "Disenchant");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NobleElephant()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A land with a chosen name can't be played")
    void landWithChosenNameCantBePlayed() {
        addReadyNullChamber(player1, "Bad River", "Noble Elephant");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BadRiver()));

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Lands and spells with other names are unaffected")
    void otherNamesAreUnaffected() {
        addReadyNullChamber(player1, "Noble Elephant", "Disenchant");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BadRiver()));

        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Bad River");
    }

    @Test
    @DisplayName("The restrictions lift when Null Chamber leaves the battlefield")
    void restrictionsLiftWhenChamberLeaves() {
        Permanent chamber = addReadyNullChamber(player1, "Noble Elephant", "Bad River");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, chamber.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NobleElephant()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    /**
     * CR 704.5k — the world rule: the older of two world permanents is put into its owner's
     * graveyard, no matter who controls them.
     */
    @Test
    @DisplayName("A second Null Chamber puts the first one into the graveyard (world rule)")
    void worldRulePutsOlderChamberIntoGraveyard() {
        Permanent first = addReadyNullChamber(player1, "Noble Elephant", "Disenchant");
        Permanent second = addReadyNullChamber(player2, "Bad River", "Disenchant");
        first.setTimestamp(1);
        second.setTimestamp(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Null Chamber");
        harness.assertOnBattlefield(player2, "Null Chamber");
    }

    @Test
    @DisplayName("A valid card name need not be present in the game")
    void canNameCardNotPresentInGame() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NullChamber()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Noble Elephant");
    }

    @Test
    @DisplayName("The as-enters choices also happen when another spell returns Null Chamber")
    void choicesAreMadeWhenReturnedToBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        NullChamber chamber = new NullChamber();
        harness.setGraveyard(player1, List.of(chamber));
        harness.setHand(player1, List.of(new Replenish()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Replenish");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Null Chamber");

        Permanent perm = findPermanent(player1, "Null Chamber");
        assertThat(perm.getChosenName()).isEqualTo("Replenish");
        assertThat(perm.getSecondChosenName()).isEqualTo("Null Chamber");
    }

    private Permanent addReadyNullChamber(Player player, String firstName, String secondName) {
        Permanent perm = new Permanent(new NullChamber());
        perm.setChosenName(firstName);
        perm.setSecondChosenName(secondName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
