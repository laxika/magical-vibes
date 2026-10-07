package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanPrimordial.class, Forest.class, GrizzlyBears.class, Plains.class,
        BreedingPool.class, DarksteelCitadel.class, PsychogenicProbe.class})
class SylvanPrimordialTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted noncreature permanent and tutors a tapped Forest")
    void destroysTargetAndFetchesForest() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        castSylvanPrimordial(List.of(plains.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(plains.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No legal target means nothing is destroyed and no search happens")
    void noLegalTargetsMeansNoSearch() {
        harness.setLibrary(player1, List.of(new Forest()));

        castSylvanPrimordial(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A legal permanent controlled by an opponent must be targeted")
    void legalOpponentPermanentMustBeTargeted() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setLibrary(player1, List.of(new Forest()));
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("each opponent if able");

        harness.castCreature(player1, 0, List.of(plains.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(plains.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent its controller controls")
    void cannotTargetOwnPermanent() {
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownPlains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target two permanents controlled by the same opponent")
    void cannotTargetTwoPermanentsOfOneOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can fetch a nonbasic Forest and puts it onto the battlefield tapped")
    void fetchesNonbasicForest() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setLibrary(player1, List.of(new BreedingPool(), new Plains()));

        castSylvanPrimordial(List.of(plains.getId()));
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isInstanceOf(BreedingPool.class);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("May fail to find a Forest even when the library contains one")
    void mayFailToFindForest() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(new Forest()));

        castSylvanPrimordial(List.of(plains.getId()));
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player2, "Plains");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An ability with all targets gone does not search or shuffle")
    void allTargetsGoneDoesNotShuffle() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(new Forest()));
        prepareCast();
        harness.castCreature(player1, 0, List.of(plains.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(plains);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Still shuffles when the legal target is indestructible")
    void shufflesWhenNothingIsDestroyed() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(new Forest()));

        castSylvanPrimordial(List.of(citadel.getId()));

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Still shuffles when no opponent has a legal target")
    void shufflesWithoutTargets() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(new Forest()));

        castSylvanPrimordial(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    private void castSylvanPrimordial(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SylvanPrimordial()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
