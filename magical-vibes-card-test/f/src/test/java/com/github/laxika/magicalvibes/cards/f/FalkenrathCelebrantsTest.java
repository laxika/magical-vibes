package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed(FalkenrathCelebrants.class)
class FalkenrathCelebrantsTest extends BaseCardTest {

    @Test
    @DisplayName("When Falkenrath Celebrants enters, two Blood tokens are created")
    void etbCreatesTwoBloodTokens() {
        harness.setHand(player1, List.of(new FalkenrathCelebrants()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bloodTokens = findPermanents(player1, "Blood");
        assertThat(bloodTokens).hasSize(2);
        assertThat(bloodTokens).allMatch(permanent -> permanent.getCard().isToken());
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    void bloodTokenDiscardsAndSacrificesAsCostsThenDrawsOnResolution() {
        harness.setHand(player1, List.of(new FalkenrathCelebrants()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        FalkenrathCelebrants discarded = new FalkenrathCelebrants();
        FalkenrathCelebrants drawn = new FalkenrathCelebrants();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void entryTriggerStillCreatesBloodAfterSourceLeaves() {
        harness.setHand(player1, List.of(new FalkenrathCelebrants()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(gd.stack).hasSize(1);
        Permanent celebrants = findPermanent(player1, "Falkenrath Celebrants");
        gd.playerBattlefields.get(player1.getId()).remove(celebrants);
        gd.playerGraveyards.get(player1.getId()).add(celebrants.getCard());

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new FalkenrathCelebrants());
        addCreatureReady(player2, new FalkenrathCelebrants());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new FalkenrathCelebrants());
        addCreatureReady(player2, new FalkenrathCelebrants());
        addCreatureReady(player2, new FalkenrathCelebrants());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}
