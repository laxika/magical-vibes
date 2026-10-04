package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieSlumberParty.class, AirElemental.class, GrizzlyBears.class})
class FaerieSlumberPartyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all creatures and creates two Faeries for each opponent who controlled one")
    void returnsCreaturesAndCreatesFaeriesForQualifyingOpponents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());

        castFaerieSlumberParty();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(GrizzlyBears.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactlyInAnyOrder(GrizzlyBears.class, AirElemental.class);
    }

    @Test
    @DisplayName("Creates no Faeries when no opponent controlled a returned creature")
    void createsNoFaeriesWithoutOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castFaerieSlumberParty();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Faeries can block flying creatures but not nonflying creatures")
    void faeriesHaveFlyingOnlyBlockingRestriction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFaerieSlumberParty();

        Permanent faerie = gd.playerBattlefields.get(player1.getId()).getFirst();
        faerie.setSummoningSick(false);
        Permanent flyingAttacker = addCreatureReady(player2, new AirElemental());
        flyingAttacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(faerie.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Faeries cannot block nonflying creatures")
    void faeriesCannotBlockNonflyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFaerieSlumberParty();

        Permanent faerie = gd.playerBattlefields.get(player1.getId()).getFirst();
        faerie.setSummoningSick(false);
        Permanent nonflyingAttacker = addCreatureReady(player2, new GrizzlyBears());
        nonflyingAttacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private void castFaerieSlumberParty() {
        harness.setHand(player1, List.of(new FaerieSlumberParty()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
