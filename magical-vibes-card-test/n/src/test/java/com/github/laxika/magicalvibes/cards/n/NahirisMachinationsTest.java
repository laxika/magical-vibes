package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahirisMachinations.class, QuilledWolf.class, DevilthornFox.class})
class NahirisMachinationsTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat on your turn, a creature you control gains indestructible until end of turn")
    void grantsIndestructibleAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());

        advanceToCombat(player1);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The indestructible grant wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger at the beginning of combat on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        harness.addToBattlefield(player1, new QuilledWolf());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 1 damage to a target blocking creature")
    void damagesBlockingCreature() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent blocker = addBlocker(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Devilthorn Fox");
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        harness.addToBattlefield(player2, new QuilledWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Quilled Wolf");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("The combat trigger targets only one creature its controller controls")
    void combatTriggerRestrictsTargetsAndGrantsOnlyToChosenCreature() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());

        advanceToCombat(player1);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());

        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chosen, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The combat trigger is removed when no creature you control can be targeted")
    void noControlledCreatureLeavesNoTriggerOnStack() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The damage ability can target a blocking creature you control on an opponent's turn")
    void canDamageOwnBlockerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent blocker = addBlocker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player1, "Devilthorn Fox");
    }

    @Test
    @DisplayName("Each activation deals only 1 damage and the ability can be activated repeatedly")
    void repeatedActivationsKillTwoToughnessBlocker() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Quilled Wolf");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
        harness.assertInGraveyard(player2, "Quilled Wolf");
    }

    @Test
    @DisplayName("The damage ability does not resolve if its target has stopped blocking")
    void targetMustStillBeBlockingAtResolution() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent blocker = addBlocker(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        blocker.getBlockingTargetIds().clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Devilthorn Fox");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The indestructible grant prevents lethal damage from destroying the chosen creature")
    void chosenCreatureSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new NahirisMachinations());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        target.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Devilthorn Fox");
        harness.assertNotInGraveyard(player1, "Devilthorn Fox");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = harness.addToBattlefieldAndReturn(owner, new DevilthornFox());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
