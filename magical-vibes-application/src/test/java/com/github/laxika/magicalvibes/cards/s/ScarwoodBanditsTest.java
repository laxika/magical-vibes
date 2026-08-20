package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarwoodBandits.class, Ornithopter.class, GrizzlyBears.class})
class ScarwoodBanditsTest extends BaseCardTest {

    @Test
    void declinesPaymentAndGainsControlOfArtifact() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        activate(bandits, artifact);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    void paymentPreventsControlChange() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        activate(bandits, artifact);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void controlReturnsWhenBanditsLeavesTheBattlefield() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        activate(bandits, artifact);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bandits));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void cannotTargetNonartifactPermanent() {
        Permanent bandits = addBandits();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bandits),
                null,
                creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBandits() {
        Permanent bandits = harness.addToBattlefieldAndReturn(player1, new ScarwoodBandits());
        bandits.setSummoningSick(false);
        return bandits;
    }

    private void activate(Permanent bandits, Permanent artifact) {
        addActivationMana();
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bandits),
                null,
                artifact.getId());
        harness.passBothPriorities();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
