package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavagePunchTest extends BaseCardTest {

    @Test
    @DisplayName("Ferocious boosts the creature before it fights")
    void ferociousBoostsBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new HillGiant());
        castSavagePunch("Grizzly Bears", "Hill Giant");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Without ferocious, the creature fights without a boost")
    void fightsWithoutFerocious() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castSavagePunch("Grizzly Bears", "Hill Giant");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The ferocious boost wears off at end of turn")
    void ferociousBoostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new LlanowarElves());
        castSavagePunch("Grizzly Bears", "Llanowar Elves");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SavagePunch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opponentGiantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBearsId, opponentGiantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SavagePunch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, giantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSavagePunch(String firstTargetName, String secondTargetName) {
        harness.setHand(player1, List.of(new SavagePunch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstTargetId = harness.getPermanentId(player1, firstTargetName);
        UUID secondTargetId = harness.getPermanentId(player2, secondTargetName);
        harness.castSorcery(player1, 0, List.of(firstTargetId, secondTargetId));
        harness.passBothPriorities();
    }
}
