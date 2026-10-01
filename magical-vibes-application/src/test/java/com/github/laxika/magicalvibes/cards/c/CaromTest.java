package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.a.AzoriusHerald;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.d.Demonfire;
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

@CardUsed({Carom.class, AzoriusFirstWing.class, AzoriusHerald.class, AzoriusSignet.class, Demonfire.class})
class CaromTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next damage to another creature and draws a card")
    void redirectsNextDamageAndDraws() {
        Demonfire drawnCard = new Demonfire();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent protectedCreature = addCreatureReady(player1, new AzoriusHerald());
        Permanent destination = addCreatureReady(player2, new AzoriusFirstWing());

        castCarom(protectedCreature.getId(), destination.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        castDemonfire(2, protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirects only one damage")
    void redirectsOnlyOneDamage() {
        Permanent protectedCreature = addCreatureReady(player1, new AzoriusHerald());
        Permanent destination = addCreatureReady(player2, new AzoriusFirstWing());

        castCarom(protectedCreature.getId(), destination.getId());

        castDemonfire(1, protectedCreature.getId());
        castDemonfire(1, protectedCreature.getId());

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirect effect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        Permanent protectedCreature = addCreatureReady(player1, new AzoriusHerald());
        Permanent destination = addCreatureReady(player2, new AzoriusFirstWing());

        castCarom(protectedCreature.getId(), destination.getId());
        declareAttackers(List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        castDemonfire(player2, 1, protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addCreatureReady(player2, new AzoriusHerald());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new Carom()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), signet.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target the same creature twice")
    void cannotUseTheSameTargetTwice() {
        Permanent creature = addCreatureReady(player2, new AzoriusHerald());
        harness.setHand(player1, List.of(new Carom()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    private void castCarom(java.util.UUID protectedId, java.util.UUID destinationId) {
        harness.setHand(player1, List.of(new Carom()));
        addMana();
        harness.castInstant(player1, 0, List.of(protectedId, destinationId));
        harness.passBothPriorities();
    }

    private void castDemonfire(int xValue, java.util.UUID targetId) {
        castDemonfire(player1, xValue, targetId);
    }

    private void castDemonfire(com.github.laxika.magicalvibes.model.Player caster, int xValue,
                               java.util.UUID targetId) {
        harness.setHand(caster, List.of(new Demonfire()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.COLORLESS, xValue);
        harness.castSorcery(caster, 0, xValue, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
