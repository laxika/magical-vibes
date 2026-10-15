package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormcallersBoon.class, GrizzlyBears.class, QasaliPridemage.class, AdarkarWastes.class})
class StormcallersBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice grants flying to creatures you control, not the opponent's")
    void grantsFlyingToOwnCreatures() {
        harness.addToBattlefield(player1, new StormcallersBoon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // The enchantment is sacrificed as a cost of the ability.
        harness.assertNotOnBattlefield(player1, "Stormcaller's Boon");
        harness.assertInGraveyard(player1, "Stormcaller's Boon");

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(findPermanent(player2, "Grizzly Bears").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new StormcallersBoon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeFlyingResolves() {
        harness.addToBattlefield(player1, new StormcallersBoon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Stormcaller's Boon");
        harness.assertInGraveyard(player1, "Stormcaller's Boon");
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();

        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainFlying() {
        harness.addToBattlefield(player1, new StormcallersBoon());
        Permanent before = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent after = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        assertThat(before.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(after.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void creaturesEnteringBeforeResolutionGainFlying() {
        harness.addToBattlefield(player1, new StormcallersBoon());
        harness.activateAbility(player1, 0, null, null);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void cascadeSkipsEqualManaValueAndCastsHitFromExileForFree() {
        StormcallersBoon equal = new StormcallersBoon();
        QasaliPridemage hit = new QasaliPridemage();
        QasaliPridemage untouched = new QasaliPridemage();
        harness.setLibrary(player1, List.of(equal, hit, untouched));

        castBoon();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(equal, hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .contains(hit.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, equal);
        assertThat(gd.exiledCards).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Qasali Pridemage");
        harness.assertNotOnBattlefield(player1, "Stormcaller's Boon");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Stormcaller's Boon");
    }

    @Test
    void decliningCascadeReturnsExiledCardsBelowUntouchedLibrary() {
        StormcallersBoon equal = new StormcallersBoon();
        QasaliPridemage hit = new QasaliPridemage();
        QasaliPridemage untouched = new QasaliPridemage();
        harness.setLibrary(player1, List.of(equal, hit, untouched));
        castBoon();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(untouched, equal, hit);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(hit.getId()));
    }

    @Test
    void cascadeWithoutQualifyingCardReturnsEntireLibrary() {
        StormcallersBoon equal = new StormcallersBoon();
        harness.setLibrary(player1, List.of(equal));
        castBoon();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equal);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cascadeSkipsLandsDespiteTheirLowerManaValue() {
        AdarkarWastes land = new AdarkarWastes();
        QasaliPridemage hit = new QasaliPridemage();
        harness.setLibrary(player1, List.of(land, hit));
        castBoon();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(land, hit);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, hit);
    }

    @Test
    void enteringWithoutCastingDoesNotCascade() {
        QasaliPridemage top = new QasaliPridemage();
        harness.setLibrary(player1, List.of(top));

        harness.addToBattlefield(player1, new StormcallersBoon());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    private void castBoon() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StormcallersBoon(), "{2}{W}{U}");
    }
}
