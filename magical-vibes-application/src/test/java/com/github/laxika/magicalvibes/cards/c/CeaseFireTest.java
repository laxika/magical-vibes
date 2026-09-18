package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DruidLyrist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CeaseFire.class, CarefulStudy.class, DruidLyrist.class, Forest.class})
class CeaseFireTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the target player from casting creatures and draws a card")
    void preventsCreatureSpellsAndDraws() {
        CarefulStudy drawnCard = new CarefulStudy();
        harness.setLibrary(player1, List.of(drawnCard));
        castCeaseFireAtPlayer2();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.setHand(player2, List.of(new Forest(), new CarefulStudy(), new DruidLyrist()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);

        List<Integer> playable = harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId());

        assertThat(playable).contains(0, 1).doesNotContain(2);
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        castCeaseFireAtPlayer2();

        harness.setHand(player2, List.of(new DruidLyrist()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId())).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Does not restrict a player other than the target")
    void doesNotRestrictNonTargetPlayer() {
        castCeaseFireAtPlayer2();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        DruidLyrist creature = new DruidLyrist();
        harness.castFromHand(player1, creature, "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        CeaseFire spell = new CeaseFire();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
    }

    private void castCeaseFireAtPlayer2() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CeaseFire()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
