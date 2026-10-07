package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.model.DayNight;
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

@CardUsed({SuspiciousStowaway.class, DawnhartRejuvenator.class})
class SuspiciousStowawayTest extends BaseCardTest {

    @Test
    @DisplayName("Suspicious Stowaway cannot be blocked")
    void cannotBeBlocked() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        stowaway.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DawnhartRejuvenator());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(stowaway)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Suspicious Stowaway draws then discards after dealing combat damage")
    void drawsThenDiscardsAfterCombatDamage() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        stowaway.setAttacking(true);
        DawnhartRejuvenator kept = new DawnhartRejuvenator();
        DawnhartRejuvenator discarded = new DawnhartRejuvenator();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(kept));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        harness.assertInGraveyard(player1, "Dawnhart Rejuvenator");
    }

    @Test
    @DisplayName("Transforms during untap when the previous active player cast no spells")
    void transformsToBackWhenNoSpellsWereCast() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        gd.spellsCastLastTurn.clear();
        gd.dayNight = DayNight.DAY;
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(stowaway.isTransformed()).isTrue();
        assertThat(stowaway.getCard()).isInstanceOf(SeafaringWerewolf.class);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Seafaring Werewolf draws a card after dealing combat damage")
    void backFaceDrawsAfterCombatDamage() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        transformToBack(stowaway);
        stowaway.setAttacking(true);
        DawnhartRejuvenator drawn = new DawnhartRejuvenator();
        harness.setLibrary(player1, List.of(drawn));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Seafaring Werewolf transforms during untap when the previous active player cast two spells")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        transformToBack(stowaway);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        gd.previousTurnActivePlayerId = player2.getId();
        harness.performUntapStep(player1);

        assertThat(stowaway.isTransformed()).isFalse();
        assertThat(stowaway.getCard()).isInstanceOf(SuspiciousStowaway.class);
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringStartsDayWhenNeitherDayNorNight() {
        gd.dayNight = DayNight.NEITHER;

        Permanent stowaway = harness.enterBattlefieldAndReturn(player1, new SuspiciousStowaway());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(stowaway.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTransformedAtNight() {
        gd.dayNight = DayNight.NIGHT;

        Permanent stowaway = harness.enterBattlefieldAndReturn(player1, new SuspiciousStowaway());

        assertThat(stowaway.isTransformed()).isTrue();
        assertThat(stowaway.getCard()).isInstanceOf(SeafaringWerewolf.class);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
    }

    @Test
    void backFaceCannotBeBlocked() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        transformToBack(stowaway);
        stowaway.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DawnhartRejuvenator());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(stowaway)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void dayboundDoesNotCreateAnUpkeepTrigger() {
        Permanent stowaway = harness.enterBattlefieldAndReturn(player1, new SuspiciousStowaway());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(stowaway.isTransformed()).isFalse();
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nightboundDoesNotCreateAnUpkeepTriggerWhenNonactivePlayerCastTwoSpells() {
        gd.dayNight = DayNight.NIGHT;
        Permanent stowaway = harness.enterBattlefieldAndReturn(player1, new SuspiciousStowaway());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeep(player1);

        assertThat(stowaway.isTransformed()).isTrue();
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        stowaway.setAttacking(true);
        DawnhartRejuvenator drawn = new DawnhartRejuvenator();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    private void transformToBack(Permanent stowaway) {
        gd.spellsCastLastTurn.clear();
        gd.dayNight = DayNight.DAY;
        gd.previousTurnActivePlayerId = player2.getId();
        harness.performUntapStep(player1);
        assertThat(stowaway.isTransformed()).isTrue();
    }
}
