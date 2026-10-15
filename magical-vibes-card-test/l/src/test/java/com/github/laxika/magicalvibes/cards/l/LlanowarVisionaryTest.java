package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarVisionary.class, Forest.class})
class LlanowarVisionaryTest extends BaseCardTest {

    @Test
    void entersAndDrawsACard() {
        harness.setHand(player1, List.of(new LlanowarVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Visionary");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void tapAbilityAddsGreenMana() {
        Permanent visionary = addCreatureReady(player1, new LlanowarVisionary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(visionary.isTapped()).isTrue();
    }

    @Test
    void manaAbilityResolvesImmediatelyForItsController() {
        Permanent visionary = addCreatureReady(player2, new LlanowarVisionary());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(visionary.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tappedVisionaryCannotActivateAgain() {
        Permanent visionary = addCreatureReady(player1, new LlanowarVisionary());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(visionary.isTapped()).isTrue();
    }

    @Test
    void summoningSickVisionaryCannotActivateManaAbility() {
        Permanent visionary = harness.addToBattlefieldAndReturn(player1, new LlanowarVisionary());
        visionary.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(visionary.isTapped()).isFalse();
    }

    @Test
    void enterTriggerDrawsExactlyOneCardOnlyAfterResolving() {
        harness.setHand(player1, List.of(new LlanowarVisionary()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Visionary");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.stack).isEmpty();
    }
}
