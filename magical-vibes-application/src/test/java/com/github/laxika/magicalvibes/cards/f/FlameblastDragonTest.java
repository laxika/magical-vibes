package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlameblastDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {X}{R} deals X damage to a chosen creature and kills it")
    void payingDealsXDamageToCreature() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3); // enough for {2}{R}

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve trigger -> prompts for X
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Paying {X}{R} deals X damage to a chosen player")
    void payingDealsXDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FlameblastDragon());
        addCreatureReady(player2, new GrizzlyBears()); // possible blocker halts combat before damage
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        // Only the trigger damage has been dealt (combat damage awaits block declaration).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Choosing X=0 declines: no damage is dealt")
    void decliningDealsNoDamage() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Without enough mana for {X}{R} the ability does nothing")
    void cannotPayDoesNothing() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1); // only pays {R}, so max X is 0

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // trigger resolves, but no X can be paid

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Attack trigger cannot target a land — any target is creature/planeswalker/player")
    void cannotTargetLand() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(bears.getId(), player1.getId(), player2.getId())
                .doesNotContain(mountain.getId());
    }

    @Test
    @DisplayName("Empty mana pool with untapped Mountains still prompts to pay {X}{R}")
    void emptyPoolWithUntappedLandsStillPromptsForX() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isGreaterThanOrEqualTo(2);
        assertThat(choice.prompt()).containsIgnoringCase("you may pay");
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
