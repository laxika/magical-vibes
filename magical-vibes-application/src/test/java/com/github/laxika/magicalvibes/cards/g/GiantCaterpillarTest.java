package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed(GiantCaterpillar.class)
class GiantCaterpillarTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices self and registers delayed Butterfly; no token yet")
    void abilityRegistersDelayedButterfly() {
        setupCaterpillar();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Giant Caterpillar");
        harness.assertInGraveyard(player1, "Giant Caterpillar");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateToken.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player1, "Butterfly");
    }

    @Test
    @DisplayName("Creates a 1/1 green Insect Butterfly with flying at next end step")
    void createsButterflyTokenAtNextEndStep() {
        setupCaterpillar();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Butterfly");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefield(player1, new GiantCaterpillar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Giant Caterpillar");
        harness.assertNotInGraveyard(player1, "Giant Caterpillar");
    }

    @Test
    @DisplayName("Creates the Butterfly at the next end step even when it is the opponent's")
    void createsButterflyAtOpponentsNextEndStep() {
        setupCaterpillar();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Butterfly");
        harness.assertNotOnBattlefield(player2, "Butterfly");
    }

    private void setupCaterpillar() {
        harness.addToBattlefield(player1, new GiantCaterpillar());
        findPermanent(player1, "Giant Caterpillar").setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
    }
}
