package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinSurprise.class, BearCub.class})
class GoblinSurpriseTest extends BaseCardTest {

    @Test
    @DisplayName("The boost mode affects only your creatures until end of turn")
    void boostsOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new BearCub());
        Permanent opponentCreature = addCreatureReady(player2, new BearCub());

        cast(0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The token mode creates two red 1/1 Goblins")
    void createsGoblinTokens() {
        cast(1);

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        });
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("The boost mode affects creatures present at resolution, not creatures entering later")
    void boostsOnlyCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new GoblinSurprise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 0, null);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BearCub());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Surprise");
    }

    @Test
    @DisplayName("The boost mode resolves with no creatures and does not create tokens")
    void boostModeResolvesOnEmptyBattlefield() {
        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Surprise");
    }

    @Test
    @DisplayName("Successive casts can choose different modes and boost the created tokens")
    void successiveCastsChooseDifferentModes() {
        Permanent ownCreature = addCreatureReady(player1, new BearCub());

        cast(1);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2);

        cast(0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getEffectivePower()).isEqualTo(3);
            assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        });
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new GoblinSurprise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, modeIndex, null);
        harness.passBothPriorities();
    }
}
