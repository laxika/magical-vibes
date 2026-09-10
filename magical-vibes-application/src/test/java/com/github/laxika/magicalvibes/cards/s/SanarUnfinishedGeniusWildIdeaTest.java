package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock, new GrizzlyBears(), ponder));

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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(shockIndex));

        harness.assertInHand(player1, "Shock");
        assertThat(sanar.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(copyId)).isNull();
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
