package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.q.QuietDisrepair;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShapeshiftersMarrow.class, FomoriNomad.class, QuietDisrepair.class})
class ShapeshiftersMarrowTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a revealed creature and permanently copies it")
    void millsCreatureAndBecomesPermanentCopy() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card creature = new FomoriNomad();
        Card nextCard = new QuietDisrepair();
        harness.setLibrary(player2, List.of(creature, nextCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gqs.isCreature(gd, marrow)).isTrue();
        assertThat(gqs.getEffectivePower(gd, marrow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, marrow)).isEqualTo(4);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Leaves a revealed noncreature on top and keeps its ability")
    void leavesNoncreatureOnTop() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card noncreature = new QuietDisrepair();
        harness.setLibrary(player2, List.of(noncreature));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, marrow)).isFalse();

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerDuringControllerUpkeep() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card creature = new FomoriNomad();
        harness.setLibrary(player1, List.of(creature));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gqs.isCreature(gd, marrow)).isFalse();
    }

    @Test
    @DisplayName("Does nothing when the opponent's library is empty")
    void doesNothingForEmptyOpponentLibrary() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        harness.setLibrary(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, marrow)).isFalse();
    }

    @Test
    @DisplayName("Still puts the revealed creature into the graveyard after the source leaves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card creature = new FomoriNomad();
        Card nextCard = new QuietDisrepair();
        harness.setLibrary(player2, List.of(creature, nextCard));

        advanceToUpkeep(player2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, marrow));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        harness.assertInHand(player1, "Shapeshifter's Marrow");
        harness.assertNotOnBattlefield(player1, "Fomori Nomad");
    }

    @Test
    @DisplayName("Each pending trigger reveals the top card at its own resolution")
    void multipleMarrowsRevealSuccessiveTopCards() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card creature = new FomoriNomad();
        Card noncreature = new QuietDisrepair();
        harness.setLibrary(player2, List.of(creature, noncreature));

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(noncreature);
        assertThat(List.of(first, second).stream().filter(p -> gqs.isCreature(gd, p)).count())
                .isEqualTo(1);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
