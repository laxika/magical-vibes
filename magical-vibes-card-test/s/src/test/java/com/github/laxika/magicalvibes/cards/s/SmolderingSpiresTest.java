package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LoamLion;
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

@CardUsed({SmolderingSpires.class, LoamLion.class})
class SmolderingSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and makes the chosen creature unable to block this turn")
    void entersTappedAndStopsTargetCreatureFromBlocking() {
        Permanent blocker = addCreatureReady(player2, new LoamLion());
        harness.setHand(player1, List.of(new SmolderingSpires()));

        harness.playLand(player1, 0);

        Permanent spires = findPermanent(player1, "Smoldering Spires");
        assertThat(spires.isTapped()).isTrue();

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tapping the land adds one red mana")
    void tapsForRedMana() {
        Permanent spires = harness.addToBattlefieldAndReturn(player1, new SmolderingSpires());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(spires.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target its controller's creature and affects only the chosen creature")
    void canTargetOwnCreatureWithoutAffectingOthers() {
        Permanent ownCreature = addCreatureReady(player1, new LoamLion());
        Permanent otherCreature = addCreatureReady(player2, new LoamLion());
        harness.setHand(player1, List.of(new SmolderingSpires()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(bls.canBlock(gd, ownCreature)).isFalse();
        assertThat(otherCreature.isCantBlockThisTurn()).isFalse();
        assertThat(bls.canBlock(gd, otherCreature)).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new LoamLion());
        harness.setHand(player1, List.of(new SmolderingSpires()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        assertThat(bls.canBlock(gd, blocker)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
        assertThat(bls.canBlock(gd, blocker)).isTrue();
    }

    @Test
    @DisplayName("Enters tapped even when there is no legal creature target")
    void entersWithoutLegalTarget() {
        harness.setHand(player1, List.of(new SmolderingSpires()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Smoldering Spires").isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land itself is not a legal creature target")
    void cannotTargetNoncreature() {
        Permanent creature = addCreatureReady(player2, new LoamLion());
        harness.setHand(player1, List.of(new SmolderingSpires()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                findPermanent(player1, "Smoldering Spires").getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }
}
