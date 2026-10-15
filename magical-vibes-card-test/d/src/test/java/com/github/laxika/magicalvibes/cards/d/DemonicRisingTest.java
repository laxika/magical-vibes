package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FarbogExplorer;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicRising.class, FarbogExplorer.class, PeelFromReality.class})
class DemonicRisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying Demon token at end step with exactly one creature")
    void createsDemonWithExactlyOneCreature() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new FarbogExplorer());

        runToEndStep();

        Permanent demon = findDemon();
        assertThat(demon).isNotNull();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().isToken()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at end step with no creatures")
    void noTriggerWithZeroCreatures() {
        harness.addToBattlefield(player1, new DemonicRising());

        beginEndStep();

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at end step with two creatures")
    void noTriggerWithTwoCreatures() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new FarbogExplorer());
        harness.addToBattlefield(player1, new FarbogExplorer());

        beginEndStep();

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's creatures count toward the condition")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new FarbogExplorer());
        harness.addToBattlefield(player2, new FarbogExplorer());
        harness.addToBattlefield(player2, new FarbogExplorer());

        runToEndStep();

        assertThat(findDemon()).isNotNull();
    }

    @Test
    @DisplayName("Does not trigger on the opponent's end step")
    void noTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new FarbogExplorer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if the only creature leaves before the trigger resolves")
    void rechecksWhenOnlyCreatureLeaves() {
        harness.addToBattlefield(player1, new DemonicRising());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FarbogExplorer());

        beginEndStep();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.assertNotOnBattlefield(player1, "Farbog Explorer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two copies trigger but only the first creates a Demon while it remains in play")
    void secondCopyRechecksCreatureCount() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new FarbogExplorer());

        beginEndStep();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(findDemon()).isNotNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Demon".equals(p.getCard().getName()))).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering a creature after the end step begins cannot create a missed trigger")
    void creatureEnteringAfterEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new DemonicRising());

        beginEndStep();
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new FarbogExplorer());
        harness.passBothPriorities();

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void runToEndStep() {
        beginEndStep();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    private Permanent findDemon() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Demon".equals(p.getCard().getName()))
                .findFirst()
                .orElse(null);
    }
}
