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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EraOfInnovationTest extends BaseCardTest {

    @Test
    void offersEnergyForAnArtifactEntry() {
        addEraOfInnovation();
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void offersEnergyForAnArtificerEntry() {
        addEraOfInnovation();
        harness.setHand(player1, List.of(new AethershieldArtificer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForAnUnmatchedPermanent() {
        addEraOfInnovation();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
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

    private Permanent addEraOfInnovation() {
        Permanent era = harness.addToBattlefieldAndReturn(player1, new EraOfInnovation());
        era.setSummoningSick(false);
        return era;
    }
}
