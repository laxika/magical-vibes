package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorningtidesLight.class, GrizzlyBears.class, GoldMyr.class, Shock.class, Forest.class})
class MorningtidesLightTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of target creatures and returns them tapped under their owners' control")
    void exilesAndReturnsTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        castMorningtidesLight(List.of(ownCreature.getId(), opponentCreature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Morningtide's Light");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.passBothPriorities();

        Permanent returnedOwnCreature = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Grizzly Bears"));
        Permanent returnedOpponentCreature = gqs.findPermanentById(gd,
                harness.getPermanentId(player2, "Gold Myr"));
        assertThat(returnedOwnCreature.isTapped()).isTrue();
        assertThat(returnedOpponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents damage to its controller until that player's next turn")
    void preventsDamageUntilNextTurn() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castMorningtidesLight(List.of());
        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithAllPlayerDamagePreventedUntilNextTurn).doesNotContain(player1.getId());
        castShock(player2, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Only creatures can be targeted")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With no targets the spell still prevents damage and exiles itself")
    void zeroTargetsStillExilesSpell() {
        castMorningtidesLight(List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Morningtide's Light");
        harness.assertNotInGraveyard(player1, "Morningtide's Light");
        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("If every target is illegal the spell does not prevent damage or exile itself")
    void allTargetsIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(creature.getId()));
        castShock(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Morningtide's Light");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).doesNotContain("Morningtide's Light");
        castShock(player2, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage prevention protects only the controller, not their creatures or opponents")
    void preventionDoesNotProtectOtherRecipients() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        castMorningtidesLight(List.of());

        castShock(player2, creature.getId());
        harness.assertInGraveyard(player1, "Gold Myr");
        castShock(player1, player2.getId());
        harness.assertLife(player2, 18);
        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Any number of creatures can be targeted, including more than ninety-nine")
    void canTargetOneHundredCreatures() {
        List<UUID> targets = java.util.stream.IntStream.range(0, 100)
                .mapToObj(index -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .toList();

        castMorningtidesLight(targets);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(100);
    }

    @Test
    @DisplayName("A remaining legal target is exiled even when another target becomes illegal")
    void partiallyIllegalTargetsStillResolve() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent doomed = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(survivor.getId(), doomed.getId()));
        castShock(player2, doomed.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Gold Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Grizzly Bears", "Morningtide's Light");
        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An exiled token ceases to exist and does not return")
    void exiledTokenDoesNotReturn() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, token);
        castMorningtidesLight(List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castMorningtidesLight(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void castShock(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
