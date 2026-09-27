package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Transluminant.class})
class TransluminantTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices Transluminant and registers delayed token creation")
    void abilitySacrificesSelfAndRegistersDelayedToken() {
        setupTransluminant();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Transluminant");
        harness.assertInGraveyard(player1, "Transluminant");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateToken.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Creates a 1/1 white Spirit with flying at the next end step")
    void createsSpiritTokenAtNextEndStep() {
        setupTransluminant();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Spirit");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }

    @Test
    @DisplayName("Activation during the end step creates the token at the following end step")
    void activationDuringEndStepWaitsForFollowingEndStep() {
        setupTransluminant();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spirit");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spirit");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not tap")
    void canActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Transluminant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Transluminant");
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new Transluminant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupTransluminant() {
        addCreatureReady(player1, new Transluminant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
    }
}
