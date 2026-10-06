package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlickSequence.class, GrizzlyBears.class, IzzetGuildmage.class})
class SlickSequenceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without drawing when it is the only spell cast this turn")
    void dealsDamageWithoutDrawingWhenItIsTheOnlySpell() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SlickSequence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage and draws a card after another spell was cast this turn")
    void dealsDamageAndDrawsAfterAnotherSpell() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SlickSequence(), new SlickSequence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell cast in response enables drawing for the original spell")
    void drawsWhenAnotherSpellIsCastInResponse() {
        harness.setLibrary(player1, List.of(new SlickSequence(), new SlickSequence()));
        harness.setHand(player1, List.of(new SlickSequence(), new SlickSequence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's spell does not enable drawing")
    void doesNotDrawForAnOpponentsSpell() {
        harness.setLibrary(player1, List.of(new SlickSequence()));
        harness.setHand(player2, List.of(new SlickSequence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new SlickSequence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell counts as another spell and a creature can be damaged")
    void drawsAfterCastingCreatureAndDealsDamageToCreature() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new SlickSequence()));
        harness.setLibrary(player1, List.of(new SlickSequence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing the only target prevents the draw even after another spell was cast")
    void doesNotDrawWhenOnlyTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new SlickSequence(), new SlickSequence()));
        harness.setHand(player1, List.of(new SlickSequence(), new SlickSequence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An uncast copy draws when its original is the only spell cast this turn")
    void uncastCopyDrawsForItsCastOriginal() {
        SlickSequence original = new SlickSequence();
        harness.addToBattlefield(player1, new IzzetGuildmage());
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(new SlickSequence(), new SlickSequence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, original.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
