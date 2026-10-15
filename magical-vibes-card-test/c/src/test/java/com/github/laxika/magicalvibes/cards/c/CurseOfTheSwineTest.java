package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfTheSwine.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class,
        Unsummon.class, SoulWarden.class})
class CurseOfTheSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X creatures and gives each exiled creature's controller a Boar")
    void exilesTargetsAndCreatesBoarsForTheirControllers() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 2, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Hill Giant");
        assertThat(findPermanents(player1, "Boar")).hasSize(1);
        assertThat(findPermanents(player2, "Boar")).hasSize(1);
        assertThat(findPermanents(player1, "Boar").getFirst().getCard().getSubtypes())
                .containsExactly(CardSubtype.BOAR);
    }

    @Test
    @DisplayName("X=0 exiles no creatures and creates no Boars")
    void xZeroDoesNothing() {
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Boar")).isEmpty();
        assertThat(findPermanents(player2, "Boar")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target more creatures than X")
    void cannotTargetMoreThanX() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Must choose exactly X creatures, not fewer")
    void cannotTargetFewerThanX() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose no targets when X is positive")
    void cannotChooseNoTargetsForPositiveX() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The same creature cannot fill two target positions")
    void cannotTargetSameCreatureTwice() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target returned to hand produces no Boar while the remaining target is exiled")
    void resolvesOnlyForRemainingLegalTarget() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Hill Giant");
        assertThat(findPermanents(player2, "Boar")).hasSize(1);
        assertThat(findPermanents(player1, "Boar")).isEmpty();
        harness.assertInGraveyard(player1, "Curse of the Swine");
    }

    @Test
    @DisplayName("No Boars are created when every target becomes illegal")
    void allTargetsIllegalCreatesNoBoars() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 1, List.of(creature.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Boar")).isEmpty();
        assertThat(findPermanents(player2, "Boar")).isEmpty();
        harness.assertInGraveyard(player1, "Curse of the Swine");
    }

    @Test
    @DisplayName("Exiling a creature token also creates a replacement Boar")
    void exilingTokenCreatesAnotherBoar() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfTheSwine(), new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveSorcery(player1, 0, 1, creature.getId());
        Permanent originalBoar = findPermanent(player2, "Boar");

        harness.castAndResolveSorcery(player1, 0, 1, originalBoar.getId());

        assertThat(findPermanents(player2, "Boar")).hasSize(1);
        Permanent replacementBoar = findPermanent(player2, "Boar");
        assertThat(replacementBoar.getId()).isNotEqualTo(originalBoar.getId());
        assertThat(replacementBoar.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, replacementBoar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, replacementBoar)).isEqualTo(2);
        assertThat(findPermanents(player1, "Boar")).isEmpty();
    }

    @Test
    @DisplayName("All targets leave before any Boars enter")
    void targetedSoulWardenDoesNotSeeBoarsEnter() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent warden = addCreatureReady(player2, new SoulWarden());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), warden.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Soul Warden");
        assertThat(findPermanents(player2, "Boar")).hasSize(2);
        harness.assertLife(player2, 20);
    }
}
