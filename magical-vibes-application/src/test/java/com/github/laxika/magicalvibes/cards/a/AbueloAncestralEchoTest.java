package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbueloAncestralEcho.class, GrizzlyBears.class, LiquimetalCoating.class, IslandSanctuary.class})
class AbueloAncestralEchoTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers another creature and returns it at the next end step")
    void flickersOwnCreature() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Abuelo, Ancestral Echo").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flickers another artifact")
    void flickersOwnArtifact() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID coatingId = harness.getPermanentId(player1, "Liquimetal Coating");

        harness.activateAbility(player1, 0, null, coatingId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liquimetal Coating");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Liquimetal Coating");
        assertThat(harness.getPermanentId(player1, "Liquimetal Coating")).isNotEqualTo(coatingId);
    }

    @Test
    @DisplayName("Cannot target itself or an invalid permanent")
    void rejectsInvalidTargets() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID abueloId = harness.getPermanentId(player1, "Abuelo, Ancestral Echo");
        UUID sanctuaryId = harness.getPermanentId(player1, "Island Sanctuary");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abueloId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sanctuaryId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void rejectsOpponentCreature() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
