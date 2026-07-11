package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StenchskipperTest extends BaseCardTest {

    private Card goblin(String name) {
        Card goblin = new Card();
        goblin.setName(name);
        goblin.setType(CardType.CREATURE);
        goblin.setSubtypes(List.of(CardSubtype.GOBLIN));
        goblin.setPower(1);
        goblin.setToughness(1);
        return goblin;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances into END_STEP
    }

    @Test
    @DisplayName("Sacrifices itself at end step when controller has no Goblins")
    void sacrificesWhenNoGoblins() {
        Permanent stench = new Permanent(new Stenchskipper());
        gd.playerBattlefields.get(player1.getId()).add(stench);

        advanceToEndStep();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stenchskipper"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Stenchskipper"));
    }

    @Test
    @DisplayName("Does not trigger when controller has a Goblin")
    void doesNotTriggerWithGoblin() {
        Permanent stench = new Permanent(new Stenchskipper());
        gd.playerBattlefields.get(player1.getId()).add(stench);
        harness.addToBattlefield(player1, goblin("Goblin Buddy"));

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stenchskipper"));
    }

    @Test
    @DisplayName("Opponent's Goblins do not prevent the sacrifice")
    void opponentGoblinsDoNotHelp() {
        Permanent stench = new Permanent(new Stenchskipper());
        gd.playerBattlefields.get(player1.getId()).add(stench);
        harness.addToBattlefield(player2, goblin("Enemy Goblin"));

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stenchskipper"));
    }

    @Test
    @DisplayName("Intervening-if re-checked: a Goblin entering before resolution stops the sacrifice")
    void conditionRecheckedAtResolution() {
        Permanent stench = new Permanent(new Stenchskipper());
        gd.playerBattlefields.get(player1.getId()).add(stench);

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, goblin("Late Goblin"));

        harness.passBothPriorities(); // resolve trigger — condition no longer met

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stenchskipper"));
    }
}
