package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArniSlaysTheTroll.class, GrizzlyBears.class, HillGiant.class})
class ArniSlaysTheTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I targets a creature you control and an opponent's creature")
    void chapterIFightsTheChosenCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArniSlaysTheTroll()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hill Giant");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(giant.getId())
                .doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId(), player1.getId())
                .doesNotContain(giant.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Chapter I allows declining its optional opposing target")
    void chapterIFightCanBeDeclined() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArniSlaysTheTroll()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hill Giant");
        harness.handlePermanentChosen(player1, giant.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Chapter II adds red mana and puts two counters on a creature you control")
    void chapterIIAddsManaAndCounters() {
        harness.addToBattlefield(player1, new ArniSlaysTheTroll());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Arni Slays the Troll");
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III gains life equal to the greatest power you control")
    void chapterIIIGainsGreatestControlledPower() {
        harness.addToBattlefield(player1, new ArniSlaysTheTroll());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Arni Slays the Troll");
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
