package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.z.ZealousConscripts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RayOfCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving untaps the target, gains control of it, and grants haste")
    void resolvesUntapGainControlAndHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("At end of turn the creature returns to its owner tapped")
    void creatureReturnsTappedAtEndOfTurn() {
        // Run on the creature owner's (player2's) turn so the cleanup control-revert is observable
        // before player2's next untap step would clear the tap.
        harness.forceActivePlayer(player2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Taps the creature immediately if another effect takes control of it")
    void tapsImmediatelyWhenAnotherEffectTakesControl() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new ZealousConscripts()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addCreatureReady(player2, new GrizzlyBears()); // valid target so spell is playable
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears()); // valid target so spell is playable
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }
}
