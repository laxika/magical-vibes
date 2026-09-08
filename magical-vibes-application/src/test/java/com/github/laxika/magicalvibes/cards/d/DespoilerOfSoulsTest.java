package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DespoilerOfSouls.class, GrizzlyBears.class, MonssGoblinRaiders.class})
class DespoilerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Despoiler of Souls cannot be declared as a blocker")
    void cannotBlock() {
        Permanent despoiler = addCreatureReady(player2, new DespoilerOfSouls());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Graveyard ability returns Despoiler of Souls to the battlefield, exiling two other creature cards")
    void graveyardAbilityReturnsSelfToBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new MonssGoblinRaiders(), new DespoilerOfSouls()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Despoiler of Souls");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Mons's Goblin Raiders"))
                .noneMatch(c -> c.getName().equals("Despoiler of Souls"));
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated without two other creature cards to exile")
    void graveyardAbilityRequiresTwoOtherCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DespoilerOfSouls()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        harness.assertInGraveyard(player1, "Despoiler of Souls");
    }
}
