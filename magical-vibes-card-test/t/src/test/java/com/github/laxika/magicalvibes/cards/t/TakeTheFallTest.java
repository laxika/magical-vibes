package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmenportVigilante;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TakeTheFall.class, GrizzlyBears.class, OmenportVigilante.class, TrainedArynx.class})
class TakeTheFallTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature -1/-0 and draws a card without an outlaw")
    void givesMinusOneMinusZeroWithoutOutlaw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Gives a target creature -4/-0 when you control an outlaw")
    void givesMinusFourMinusZeroWithOutlaw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card outlaw = new GrizzlyBears();
        outlaw.setSubtypes(List.of(CardSubtype.ROGUE));
        harness.addToBattlefield(player1, outlaw);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Checks for an outlaw as the spell resolves")
    void checksOutlawAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card outlaw = new GrizzlyBears();
        outlaw.setSubtypes(List.of(CardSubtype.ROGUE));
        harness.addToBattlefield(player1, outlaw);
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's outlaw does not upgrade the reduction")
    void opponentsOutlawDoesNotUpgradeReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OmenportVigilante());
        Card drawn = new TrainedArynx();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("An outlaw entering after casting upgrades the reduction at resolution")
    void outlawEnteringBeforeResolutionUpgradesReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArynx());
        Card drawn = new TrainedArynx();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new OmenportVigilante());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Your targeted outlaw enables the upgrade and the reduction expires at cleanup")
    void targetedOutlawEnablesUpgradeUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        Card drawn = new TrainedArynx();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not draw when the sole target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArynx());
        Card drawn = new TrainedArynx();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new TakeTheFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Take the Fall");
    }
}
