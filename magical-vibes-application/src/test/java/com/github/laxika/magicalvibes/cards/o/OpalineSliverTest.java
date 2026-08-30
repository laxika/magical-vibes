package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalineSliver.class, MetallicSliver.class, Shock.class, ElaborateFirecannon.class})
class OpalineSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver's controller may draw when an opponent's spell targets it")
    void controllerMayDrawWhenOpponentSpellTargetsSliver() {
        harness.addToBattlefield(player1, new OpalineSliver());
        var sliverId = harness.getPermanentId(player1, "Opaline Sliver");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        beginTurn(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, sliverId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The granted ability applies to Slivers controlled by another player")
    void grantsAbilityToOpponentsSlivers() {
        harness.addToBattlefield(player1, new OpalineSliver());
        harness.addToBattlefield(player2, new MetallicSliver());
        var sliverId = harness.getPermanentId(player2, "Metallic Sliver");
        int handBefore = gd.playerHands.get(player2.getId()).size();

        beginTurn(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sliverId);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The granted ability does not trigger for its controller's spell")
    void doesNotTriggerForOwnSpell() {
        harness.addToBattlefield(player1, new OpalineSliver());
        var sliverId = harness.getPermanentId(player1, "Opaline Sliver");

        beginTurn(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sliverId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The granted ability does not trigger for an opponent's activated ability")
    void doesNotTriggerForOpponentAbility() {
        harness.addToBattlefield(player1, new OpalineSliver());
        var sliverId = harness.getPermanentId(player1, "Opaline Sliver");

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, sliverId);

        assertThat(gd.stack).hasSize(1);
    }

    private void beginTurn(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
