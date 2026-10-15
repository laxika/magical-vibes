package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GraniticTitan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WharfInfiltrator.class, Censor.class, Forest.class, GraniticTitan.class,
        GrizzlyBears.class, HillGiant.class})
class WharfInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Skulk prevents a greater-power creature from blocking")
    void skulkPreventsGreaterPowerBlocker() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent infiltrator = addCreatureReady(player1, new WharfInfiltrator());
        infiltrator.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infiltrator);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("Combat damage can be accepted to draw and discard")
    void combatDamageDrawsThenDiscards() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new WharfInfiltrator()).setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Discarding a creature offers the Eldrazi Horror token")
    void discardingCreatureCanCreateToken() {
        harness.addToBattlefield(player1, new WharfInfiltrator());
        harness.setHand(player1, List.of(new GraniticTitan()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.ELDRAZI)
                        && p.getCard().getSubtypes().contains(CardSubtype.HORROR)
                        && p.getEffectivePower() == 3
                        && p.getEffectiveToughness() == 2);
    }

    @Test
    @DisplayName("Declining the token payment creates no Eldrazi Horror")
    void decliningTokenPaymentCreatesNoToken() {
        harness.addToBattlefield(player1, new WharfInfiltrator());
        harness.setHand(player1, List.of(new GraniticTitan()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.ELDRAZI));
    }

    @Test
    @DisplayName("Discarding a noncreature does not offer the token")
    void discardingNoncreatureDoesNotTriggerToken() {
        harness.addToBattlefield(player1, new WharfInfiltrator());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.ELDRAZI));
    }

    @Test
    @DisplayName("Skulk allows an equal-power creature to block")
    void equalPowerCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new WharfInfiltrator());
        Permanent attacker = addCreatureReady(player1, new WharfInfiltrator());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the combat damage draw also skips the discard")
    void decliningDrawDoesNotDiscard() {
        harness.setHand(player1, List.of(new WharfInfiltrator()));
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new WharfInfiltrator()).setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Wharf Infiltrator");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature discard does not trigger the token ability")
    void opponentDiscardDoesNotTriggerToken() {
        harness.addToBattlefield(player1, new WharfInfiltrator());
        harness.setHand(player2, List.of(new GraniticTitan()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Granitic Titan");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }
}
