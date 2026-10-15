package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RiverDarter;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.a.AggressiveUrge;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SwiftWarden.class, RiverDarter.class, RaptorCompanion.class, AggressiveUrge.class})
class SwiftWardenTest extends BaseCardTest {

    @Test
    void canTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent warden = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, warden.getId());
        resolveAllTriggers();

        assertThat(warden.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    void cannotTargetNonMerfolk() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Merfolk you control");
    }

    @Test
    void hexproofPreventsOpponentTargetingButAllowsControllerTargeting() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, merfolk.getId());
        resolveAllTriggers();

        harness.setHand(player2, List.of(new AggressiveUrge()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new AggressiveUrge()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, merfolk.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB gives a Merfolk you control hexproof until end of turn")
    void etbGrantsHexproofToTargetMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, merfolk.getId());
        resolveAllTriggers();

        assertThat(merfolk.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Granted hexproof wears off at end of turn")
    void grantedHexproofWearsOffAtEndOfTurn() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, merfolk.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(merfolk.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target an opponent's Merfolk")
    void cannotTargetOpponentMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new RiverDarter());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Merfolk you control");
    }

    @Test
    @DisplayName("Can cast during the opponent's turn because it has flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        gs.passPriority(gd, player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
