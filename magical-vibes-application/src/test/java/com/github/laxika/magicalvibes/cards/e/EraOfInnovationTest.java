package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AethershieldArtificer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EraOfInnovation.class, AethershieldArtificer.class, Forest.class,
        GrizzlyBears.class, Ornithopter.class})
class EraOfInnovationTest extends BaseCardTest {

    @Test
    void offersEnergyForAnArtifactEntry() {
        addEraOfInnovation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void offersEnergyForAnArtificerEntry() {
        addEraOfInnovation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new AethershieldArtificer(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForAnOpponentArtifactEntry() {
        addEraOfInnovation();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Ornithopter(), "{0}");

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForAnUnmatchedPermanent() {
        addEraOfInnovation();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysEnergySacrificesAndDrawsThreeCards() {
        Permanent era = addEraOfInnovation();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(era);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    void cannotActivateWithoutSixEnergyCounters() {
        addEraOfInnovation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("six energy counters");
    }

    @Test
    void decliningPaymentDoesNotGrantEnergyOrSpendMana() {
        addEraOfInnovation();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingForEntryAddsExactlyTwoEnergyAndSpendsOneMana() {
        addEraOfInnovation();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationPaysCostsImmediatelyAndKeepsSurplusEnergy() {
        Permanent era = addEraOfInnovation();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        gd.playerEnergyCounters.put(player1.getId(), 8);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(era);
        harness.assertInGraveyard(player1, "Era of Innovation");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void fiveEnergyCannotPayTheActivationCost() {
        Permanent era = addEraOfInnovation();
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("six energy counters");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(era);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addEraOfInnovation() {
        return harness.addToBattlefieldAndReturn(player1, new EraOfInnovation());
    }
}
