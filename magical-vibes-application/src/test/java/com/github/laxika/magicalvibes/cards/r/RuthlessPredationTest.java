package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessPredation.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Unsummon.class})
class RuthlessPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creature before it fights")
    void boostsBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(bearsId, giantId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(bears.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearsId, elvesId));

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opponentElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBearsId, opponentElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        UUID bearsId = battlefield.get(0).getId();
        UUID elvesId = battlefield.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillBoostsWhenOpposingCreatureLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castSorcery(player1, 0, List.of(bears.getId(), giantId));
        harness.castAndResolveInstant(player2, 0, giantId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Ruthless Predation");
    }

    @Test
    void noFightWhenYourCreatureLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent giant = findPermanent(player2, "Hill Giant");

        harness.castSorcery(player1, 0, List.of(bearsId, giant.getId()));
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ruthless Predation");
    }

    @Test
    void noBoostOrFightWhenFirstTargetChangesController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent giant = findPermanent(player2, "Hill Giant");

        harness.castSorcery(player1, 0, List.of(bears.getId(), giant.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Ruthless Predation");
    }

    @Test
    void stillBoostsButDoesNotFightWhenSecondTargetChangesController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuthlessPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent giant = findPermanent(player2, "Hill Giant");

        harness.castSorcery(player1, 0, List.of(bears.getId(), giant.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(giant);
        gd.playerBattlefields.get(player1.getId()).add(giant);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Ruthless Predation");
    }
}
