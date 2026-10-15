package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({SanarUnfinishedGeniusWildIdea.class, Shock.class, Ponder.class, GrizzlyBears.class})
class SanarUnfinishedGeniusWildIdeaTest extends BaseCardTest {

    @Test
    @DisplayName("Sanar enters prepared and creates a Wild Idea copy in exile")
    void entersPrepared() {
        Permanent sanar = castSanar();

        assertThat(sanar.isPrepared()).isTrue();
        UUID copyId = sanar.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Sanar cannot create a Treasure before an instant or sorcery is cast")
    void treasureAbilityRequiresInstantOrSorcery() {
        Permanent sanar = addReadySanar();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast an instant or sorcery spell");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(sanar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("After an instant is cast, Sanar creates a Treasure token")
    void createsTreasureAfterInstantIsCast() {
        Permanent sanar = addReadySanar();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(sanar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Wild Idea searches for an instant or sorcery and puts it into hand")
    void wildIdeaSearchesForInstantOrSorcery() {
        Permanent sanar = castSanar();
        UUID copyId = sanar.getPreparedSpellCardId();
        Card shock = new Shock();
        Card ponder = new Ponder();
        harness.setLibrary(player1, List.of(shock, new GrizzlyBears(), ponder));

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Ponder");

        int shockIndex = search.params().cards().indexOf(shock);
        harness.handleCardChosen(player1, shockIndex);

        harness.assertInHand(player1, "Shock");
        assertThat(sanar.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("Entering prepared does not put a triggered ability on the stack")
    void entersPreparedWithoutTriggeredAbility() {
        harness.setHand(player1, List.of(new SanarUnfinishedGeniusWildIdea()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sanar = findPermanent(player1, "Sanar, Unfinished Genius");
        assertThat(sanar.isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sanar can create Treasure while its controller's instant is still on the stack")
    void treasureDoesNotRequireSpellToResolve() {
        Permanent sanar = addReadySanar();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(sanar.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's instant does not enable Sanar's Treasure ability")
    void opponentsSpellDoesNotEnableTreasure() {
        Permanent sanar = addReadySanar();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast an instant or sorcery spell");
        assertThat(sanar.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Casting Wild Idea unprepares Sanar immediately and enables Treasure")
    void castingPreparedSorceryEnablesTreasure() {
        Permanent sanar = castSanar();
        sanar.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Ponder()));
        addWildIdeaMana();
        harness.castFromExile(player1, sanar.getPreparedSpellCardId());

        assertThat(sanar.isPrepared()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Ponder");
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Wild Idea may fail to find even when a matching card exists")
    void wildIdeaCanFailToFind() {
        Permanent sanar = castSanar();
        Card ponder = new Ponder();
        harness.setLibrary(player1, List.of(ponder));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWildIdeaMana();
        harness.castFromExile(player1, sanar.getPreparedSpellCardId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Ponder");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ponder);
        assertThat(sanar.isPrepared()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A failed attempt to pay for Wild Idea leaves Sanar prepared")
    void insufficientManaDoesNotUnprepare() {
        Permanent sanar = castSanar();
        UUID copyId = sanar.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sanar.isPrepared()).isTrue();
        assertThat(sanar.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @CardUsed({SlipOutTheBack.class})
    @DisplayName("Phasing Sanar out removes its prepare-spell copy without unpreparing it")
    void phasingOutRemovesPreparedCopy() {
        Permanent sanar = castSanar();
        UUID copyId = sanar.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, sanar.getId());

        harness.assertNotOnBattlefield(player1, "Sanar, Unfinished Genius");
        assertThat(sanar.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNull();
        addWildIdeaMana();
        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wild Idea keeps normal sorcery timing while Sanar is prepared")
    void wildIdeaCannotBeCastOnOpponentsTurn() {
        Permanent sanar = castSanar();
        UUID copyId = sanar.getPreparedSpellCardId();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWildIdeaMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sanar.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @CardUsed({SlipOutTheBack.class})
    @DisplayName("Sanar receives a new Wild Idea copy when it phases back in prepared")
    void phasingInCreatesNewPreparedCopy() {
        Permanent sanar = castSanar();
        UUID oldCopyId = sanar.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, sanar.getId());
        harness.performUntapStep(player1);

        harness.assertOnBattlefield(player1, "Sanar, Unfinished Genius");
        assertThat(sanar.isPrepared()).isTrue();
        UUID newCopyId = sanar.getPreparedSpellCardId();
        assertThat(newCopyId).isNotNull().isNotEqualTo(oldCopyId);
        assertThat(gd.findExiledCard(oldCopyId)).isNull();
        assertThat(gd.findExiledCard(newCopyId)).isNotNull();
    }

    private void addWildIdeaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent castSanar() {
        harness.setHand(player1, List.of(new SanarUnfinishedGeniusWildIdea()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Sanar, Unfinished Genius");
    }

    private Permanent addReadySanar() {
        Permanent sanar = harness.addToBattlefieldAndReturn(player1, new SanarUnfinishedGeniusWildIdea());
        sanar.setSummoningSick(false);
        return sanar;
    }
}
