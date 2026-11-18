package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({CoordinatedCharge.class, AlmightyBrushwagg.class})
class CoordinatedChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all creatures controlled by the caster")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addToBattlefield(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> {
                    if (permanent.getCard().hasType(CardType.CREATURE)) {
                        assertThat(PermanentGrants.powerBoostOf(gd, permanent)).isEqualTo(2);
                        assertThat(PermanentGrants.toughnessBoostOf(gd, permanent)).isEqualTo(1);
                    }
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allSatisfy(permanent -> {
                    if (permanent.getCard().hasType(CardType.CREATURE)) {
                        assertThat(PermanentGrants.powerBoostOf(gd, permanent)).isZero();
                        assertThat(PermanentGrants.toughnessBoostOf(gd, permanent)).isZero();
                    }
                });
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coordinated Charge");
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotThoseEnteringLater() {
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstant(player1, 0);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());

        assertThat(PermanentGrants.powerBoostOf(gd, beforeResolution)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, beforeResolution)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, afterResolution)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, afterResolution)).isZero();
    }

    @Test
    void resolvesWithNoCreatures() {
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Coordinated Charge");
    }

    @Test
    void cyclingPaysCostsImmediatelyAndDoesNotBoostCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Coordinated Charge");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
    }

    @Test
    void cannotCycleWithoutTwoMana() {
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Coordinated Charge");
        harness.assertNotInGraveyard(player1, "Coordinated Charge");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
