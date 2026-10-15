package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushedRebirth.class, AirElemental.class, CruelEdict.class, FountainOfYouth.class,
        GrizzlyBears.class, SerraAngel.class})
class RushedRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a lower-mana-value creature from the library onto the battlefield tapped when the target dies")
    void findsLowerManaValueCreatureWhenTargetDies() {
        harness.addToBattlefield(player2, new AirElemental());
        GrizzlyBears lowerCreature = new GrizzlyBears();
        AirElemental sameManaValueCreature = new AirElemental();
        harness.setLibrary(player1, List.of(lowerCreature, sameManaValueCreature));
        harness.setHand(player1, List.of(new RushedRebirth(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Air Elemental"));

        harness.castSorcery(player1, 0, player2.getId());
        resolveStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent found = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == lowerCreature)
                .findFirst()
                .orElseThrow();
        assertThat(found.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sameManaValueCreature);
    }

    @Test
    @DisplayName("Does not find a creature with equal or greater mana value")
    void requiresStrictlyLowerManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears sameManaValueCreature = new GrizzlyBears();
        SerraAngel higherManaValueCreature = new SerraAngel();
        harness.setLibrary(player1, List.of(sameManaValueCreature, higherManaValueCreature));
        harness.setHand(player1, List.of(new RushedRebirth(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.castSorcery(player1, 0, player2.getId());
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(sameManaValueCreature, higherManaValueCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RushedRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can fail to find even with an eligible creature in the library")
    void canDeclineRestrictedSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligible));
        harness.setHand(player1, List.of(new RushedRebirth(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castSorcery(player1, 0, player2.getId());
        resolveStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each Rushed Rebirth on the same creature produces its own search")
    void multipleSpellsProduceSeparateSearches() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RushedRebirth(), new RushedRebirth(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castSorcery(player1, 0, player2.getId());
        resolveStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        resolveStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The delayed trigger expires when the turn ends")
    void deathOnLaterTurnDoesNotSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligible));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RushedRebirth()));
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
