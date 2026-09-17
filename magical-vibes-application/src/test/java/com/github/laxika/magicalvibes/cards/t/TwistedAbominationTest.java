package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistedAbomination.class, Swamp.class})
class TwistedAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Black mana grants Twisted Abomination a regeneration shield")
    void blackManaActivatesRegeneration() {
        Permanent abomination = addCreatureReady(player1, new TwistedAbomination());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(abomination.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Twisted Abomination from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent blocker = addCreatureReady(player1, new TwistedAbomination());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new TwistedAbomination());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getRegenerationShield()).isZero();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargets()).isEmpty();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(blocker.getCard().getId()));
    }

    @Test
    @DisplayName("Swampcycling searches for a Swamp and discards Twisted Abomination")
    void swampcyclingSearchesForSwamp() {
        TwistedAbomination abomination = new TwistedAbomination();
        harness.setHand(player1, List.of(abomination));
        harness.setLibrary(player1, List.of(new TwistedAbomination(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Swamp);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
        harness.assertInGraveyard(player1, "Twisted Abomination");
    }

    @Test
    @DisplayName("Swampcycling may fail to find a Swamp and still discards Twisted Abomination")
    void swampcyclingMayFailToFindSwamp() {
        TwistedAbomination abomination = new TwistedAbomination();
        TwistedAbomination nonSwamp = new TwistedAbomination();
        harness.setHand(player1, List.of(abomination));
        harness.setLibrary(player1, List.of(nonSwamp));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Twisted Abomination");
        harness.assertNotInHand(player1, "Twisted Abomination");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonSwamp);
    }
}
