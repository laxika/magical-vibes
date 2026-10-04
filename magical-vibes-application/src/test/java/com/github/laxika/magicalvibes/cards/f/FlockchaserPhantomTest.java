package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlockchaserPhantom.class, SolRing.class})
class FlockchaserPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives the next spell convoke and consumes the grant")
    void attackingGrantsConvokeToNextSpell() {
        Permanent phantom = addCreatureReady(player1, new FlockchaserPhantom());
        Permanent creature = addCreatureReady(player1, new FlockchaserPhantom());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(phantom)));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID creatureId = creature.getId();
        harness.setHand(player1, List.of(new SolRing()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creatureId));

        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        creature.untap();
        harness.setHand(player1, List.of(new SolRing()));
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attacking Phantom can convoke the next spell because vigilance leaves it untapped")
    void attackingPhantomCanConvoke() {
        Permanent phantom = addCreatureReady(player1, new FlockchaserPhantom());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SolRing()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(phantom.getId()));
        resolveAllTriggers();

        assertThat(phantom.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    @DisplayName("Casting the next spell entirely with mana still consumes the convoke grant")
    void manaPaymentConsumesGrant() {
        Permanent phantom = addCreatureReady(player1, new FlockchaserPhantom());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SolRing(), new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(phantom.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(phantom.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two attack triggers apply to the same next spell, not successive spells")
    void multipleGrantsAreConsumedTogether() {
        Permanent first = addCreatureReady(player1, new FlockchaserPhantom());
        Permanent second = addCreatureReady(player1, new FlockchaserPhantom());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SolRing(), new SolRing()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(first.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves the battlefield")
    void sourceLeavingDoesNotPreventGrant() {
        Permanent phantom = addCreatureReady(player1, new FlockchaserPhantom());
        Permanent helper = addCreatureReady(player1, new FlockchaserPhantom());
        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(phantom);
        gd.playerGraveyards.get(player1.getId()).add(phantom.getCard());
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SolRing()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(helper.getId()));
        resolveAllTriggers();

        assertThat(helper.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    @DisplayName("Flockchaser Phantom itself can be cast using convoke for colored and generic mana")
    void intrinsicConvokePaysEntireCostWithSummoningSickCreatures() {
        List<Permanent> helpers = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new FlockchaserPhantom()))
                .toList();
        harness.setHand(player1, List.of(new FlockchaserPhantom()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                helpers.stream().map(Permanent::getId).toList());
        resolveAllTriggers();

        assertThat(helpers).allMatch(Permanent::isTapped);
        assertThat(countPermanents(player1, "Flockchaser Phantom")).isEqualTo(7);
    }

    @Test
    @DisplayName("An unused convoke grant expires when the turn ends")
    void unusedGrantExpiresAtEndOfTurn() {
        Permanent phantom = addCreatureReady(player1, new FlockchaserPhantom());
        harness.setLibrary(player1, List.of(new SolRing(), new SolRing()));
        harness.setLibrary(player2, List.of(new SolRing(), new SolRing()));
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(phantom.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
