package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("In the Trenches")
class InTheTrenchesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control and not creatures controlled by an opponent")
    void boostsOnlyOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        int ownPowerBefore = gqs.getEffectivePower(gd, ownCreature);
        int ownToughnessBefore = gqs.getEffectiveToughness(gd, ownCreature);
        int opponentPowerBefore = gqs.getEffectivePower(gd, opponentCreature);
        int opponentToughnessBefore = gqs.getEffectiveToughness(gd, opponentCreature);
        addSource();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(ownPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(ownToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(opponentPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(opponentToughnessBefore);
    }

    @Test
    @DisplayName("Exiles one opposing nonland permanent until In the Trenches leaves")
    void exilesTargetUntilSourceLeaves() {
        Permanent source = addSource();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        activate(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ResoundingWave()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can only be activated once and at sorcery speed")
    void activationRestrictions() {
        addSource();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        addAbilityMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        addSource();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private Permanent addSource() {
        Permanent source = new Permanent(new InTheTrenches());
        source.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return source;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
