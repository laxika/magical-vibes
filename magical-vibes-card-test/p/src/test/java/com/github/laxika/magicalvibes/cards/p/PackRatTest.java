package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CatacombSlug;
import com.github.laxika.magicalvibes.cards.d.DrainpipeVermin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PackRat.class, CatacombSlug.class, DrainpipeVermin.class})
class PackRatTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void setupMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Power and toughness equal the number of Rats you control, counting itself")
    void powerToughnessCountsRats() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);

        harness.addToBattlefieldAndReturn(player1, new PackRat());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rats controlled by an opponent are not counted")
    void opponentRatsDoNotCount() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.addToBattlefieldAndReturn(player2, new PackRat());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
    }

    @Test
    @DisplayName("{2}{B}, Discard a card creates a token copy that grows the whole pack")
    void activationCreatesTokenCopy() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.setHand(player1, List.of(new CatacombSlug()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Catacomb Slug");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        setupMainPhase();
        harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.setHand(player1, new ArrayList<>());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsOtherRatCreaturesButNotNonRats() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.addToBattlefield(player1, new DrainpipeVermin());
        harness.addToBattlefield(player1, new CatacombSlug());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    @Test
    void tokenCopyCanActivateWhileSummoningSick() {
        setupMainPhase();
        harness.addToBattlefield(player1, new PackRat());
        harness.setHand(player1, List.of(new PackRat(), new PackRat()));
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        addActivationMana();
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Pack Rat")).isEqualTo(3);
        for (Permanent rat : findPermanents(player1, "Pack Rat")) {
            assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(3);
        }
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void createsCopyEvenAfterSourceLeavesBattlefield() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.setHand(player1, List.of(new PackRat()));
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, rat));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Pack Rat");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void characteristicAbilityWorksInHandAndGraveyard() {
        setupMainPhase();
        PackRat handRat = new PackRat();
        PackRat graveyardRat = new PackRat();
        harness.setHand(player1, List.of(handRat));
        harness.setGraveyard(player1, List.of(graveyardRat));

        assertThat(gqs.getEffectiveCardPower(gd, handRat)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, graveyardRat)).isZero();

        harness.addToBattlefield(player1, new DrainpipeVermin());
        harness.addToBattlefield(player2, new PackRat());

        assertThat(gqs.getEffectiveCardPower(gd, handRat)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, handRat)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, graveyardRat)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, graveyardRat)).isEqualTo(1);
    }

    @Test
    void tokenDoesNotCopyCountersOnSource() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        rat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new PackRat()));
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Pack Rat").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
}
