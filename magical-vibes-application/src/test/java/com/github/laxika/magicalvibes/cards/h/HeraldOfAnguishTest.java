package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfAnguish.class, GrizzlyBears.class, Ornithopter.class, HillGiant.class})
class HeraldOfAnguishTest extends BaseCardTest {

    @Test
    @DisplayName("At the controller's end step, each opponent discards a card")
    void opponentsDiscardAtControllerEndStep() {
        harness.addToBattlefield(player1, new HeraldOfAnguish());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> "Grizzly Bears".equals(card.getName()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new HeraldOfAnguish());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing an artifact gives target creature -2/-2 until end of turn")
    void sacrificesArtifactAndWeakensTargetCreature() {
        harness.addToBattlefield(player1, new HeraldOfAnguish());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new HeraldOfAnguish());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Improvise can pay all five generic mana using summoning-sick artifacts")
    void castsWithImprovise() {
        harness.setHand(player1, List.of(new HeraldOfAnguish()));
        List<Permanent> artifacts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            artifacts.add(harness.addToBattlefieldAndReturn(player1, new Ornithopter()));
        }
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureTappingPermanents(player1, 0,
                artifacts.stream().map(Permanent::getId).toList());

        assertThat(artifacts).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Herald of Anguish");
    }

    @Test
    @DisplayName("Improvise cannot pay the black mana requirement")
    void improviseCannotPayColoredMana() {
        harness.setHand(player1, List.of(new HeraldOfAnguish()));
        List<Permanent> artifacts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            artifacts.add(harness.addToBattlefieldAndReturn(player1, new Ornithopter()));
        }
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                artifacts.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty opponent hand does not stall the end-step trigger")
    void emptyOpponentHand() {
        harness.addToBattlefield(player1, new HeraldOfAnguish());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped artifact is sacrificed as a cost before the ability resolves")
    void sacrificesTappedArtifactBeforeResolutionAndCanTargetItself() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfAnguish());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, herald.getId());

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(herald.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, herald)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, herald)).isEqualTo(3);

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(herald.getPowerModifier()).isZero();
        assertThat(herald.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The toughness reduction puts a creature with zero toughness into its owner's graveyard")
    void killsCreatureWithTwoToughness() {
        harness.addToBattlefield(player1, new HeraldOfAnguish());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfAnguish());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, herald.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }
}
