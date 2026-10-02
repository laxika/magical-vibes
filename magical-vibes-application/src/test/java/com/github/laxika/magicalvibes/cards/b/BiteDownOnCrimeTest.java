package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BiteDownOnCrime.class, GrizzlyBears.class, HillGiant.class})
class BiteDownOnCrimeTest extends BaseCardTest {

    @Test
    void boostsControlledCreatureBeforeItDealsPowerDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(attacker.getId(), victim.getId()));

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void collectingEvidenceReducesCostByTwo() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(attacker.getId(), victim.getId()), List.of(), false,
                null, null, null, null, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotTargetControlledCreatureAsTheVictim() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void stillBoostsCreatureWhenVictimLeavesBeforeResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(attacker.getId(), victim.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(gd, victim);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Bite Down on Crime");
    }

    @Test
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(attacker.getId(), victim.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(gd, attacker);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bite Down on Crime");
    }

    @Test
    void rejectsEvidenceWithTotalManaValueBelowSix() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(attacker.getId(), victim.getId()), List.of(), false,
                null, null, null, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("collect evidence 6");

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Bite Down on Crime");
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDownOnCrime()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(attacker.getId(), victim.getId()));
        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
