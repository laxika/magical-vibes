package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CustodiLich.class, GrizzlyBears.class})
class CustodiLichTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes the monarch and makes a target player sacrifice a creature")
    void becomesMonarchAndSacrificesTargetPlayersCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new CustodiLich());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Entering while already monarch does not trigger a sacrifice")
    void alreadyMonarchDoesNotSacrifice() {
        gd.monarchPlayerId = player1.getId();
        Permanent lich = harness.enterBattlefieldAndReturn(player1, new CustodiLich());

        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lich);
    }

    @Test
    @DisplayName("The controller can target themselves and sacrifice Custodi Lich")
    void canTargetSelfAndSacrificeSource() {
        Permanent lich = harness.enterBattlefieldAndReturn(player1, new CustodiLich());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lich);
        harness.assertInGraveyard(player1, "Custodi Lich");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player without creatures is a legal target and sacrifices nothing")
    void targetWithoutCreaturesSacrificesNothing() {
        Permanent lich = harness.enterBattlefieldAndReturn(player1, new CustodiLich());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lich);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The targeted player chooses which of their creatures to sacrifice")
    void targetedPlayerChoosesCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CustodiLich());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CustodiLich());
        harness.enterBattlefieldAndReturn(player1, new CustodiLich());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.playerId()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player2, "Custodi Lich");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }
}
