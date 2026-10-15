package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdherentsHeirloom.class, GrizzlyBears.class, LlanowarElves.class, Naturalize.class})
class AdherentsHeirloomTest extends BaseCardTest {

    @Test
    void seeksCreatureOfMostPrevalentCreatureTypeInControllersLibrary() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setLibrary(player2, List.of(
                new LlanowarElves(), new LlanowarElves(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new AdherentsHeirloom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
    }

    @Test
    void producesCreatureSpellOnlyMana() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new AdherentsHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(heirloom.isTapped()).isTrue();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    void determinesMostPrevalentTypeWhenTriggerResolves() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.enterBattlefieldAndReturn(player1, new AdherentsHeirloom());
        assertThat(gd.stack).hasSize(1);

        // Model a response changing the library before the enter trigger resolves.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new LlanowarElves()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Llanowar Elves");
    }

    @Test
    void seeksEvenIfHeirloomIsDestroyedInResponse() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent heirloom = harness.enterBattlefieldAndReturn(player1, new AdherentsHeirloom());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, heirloom.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Adherent's Heirloom");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Grizzly Bears");
    }

    @Test
    void doesNothingWhenLibraryContainsNoCreatures() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AdherentsHeirloom()));
        harness.enterBattlefieldAndReturn(player1, new AdherentsHeirloom());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void manaCanPayForCreatureSpell() {
        harness.addToBattlefield(player1, new AdherentsHeirloom());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isZero();
    }

    @Test
    void manaCannotPayForArtifactSpell() {
        harness.addToBattlefield(player1, new AdherentsHeirloom());
        harness.setHand(player1, List.of(new AdherentsHeirloom()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Adherent's Heirloom");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(1);
    }
}
