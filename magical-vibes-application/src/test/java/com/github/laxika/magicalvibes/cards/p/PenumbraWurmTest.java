package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PenumbraWurm.class)
class PenumbraWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Penumbra Wurm dies, it creates a 6/6 black Wurm token with trample")
    void deathCreatesTramplingWurmToken() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PenumbraWurm());
        wurm.setMarkedDamage(6);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Wurm");
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WURM);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The controller of Penumbra Wurm controls the token it creates when it dies")
    void deathCreatesTokenForDyingWurmsController() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new PenumbraWurm());
        wurm.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Wurm")).hasSize(1);
        assertThat(findPermanents(player1, "Wurm")).isEmpty();
    }

    @Test
    @DisplayName("The Wurm token is created only when the death trigger resolves")
    void tokenCreationWaitsForTriggerResolution() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PenumbraWurm());
        wurm.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Penumbra Wurm");
        assertThat(findPermanents(player1, "Wurm")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wurm")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created Wurm token does not create another token when it dies")
    void tokenDoesNotInheritDeathAbility() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PenumbraWurm());
        wurm.setMarkedDamage(6);
        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Wurm");
        token.setMarkedDamage(6);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wurm")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Penumbra Wurm creates one token when two die simultaneously")
    void simultaneousDeathsEachCreateOneToken() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PenumbraWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PenumbraWurm());
        first.setMarkedDamage(6);
        second.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Penumbra Wurm")).isEmpty();
        assertThat(findPermanents(player1, "Wurm")).hasSize(2);
        assertThat(findPermanents(player2, "Wurm")).isEmpty();
    }
}
