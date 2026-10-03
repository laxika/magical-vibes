package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChupacabraEcho.class, GrizzlyBears.class, Shock.class, Abrade.class, Swamp.class})
class ChupacabraEchoTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -X/-X for permanent cards in your graveyard")
    void etbUsesPermanentCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castChupacabra(target);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not count permanent cards in an opponent's graveyard")
    void etbDoesNotUseOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castChupacabra(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castChupacabra(target);
        assertThat(target.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChupacabraEcho()));
        addChupacabraMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("ETB does not trigger without an opponent creature target")
    void etbDoesNotTriggerWithoutOpponentCreature() {
        harness.setHand(player1, List.of(new ChupacabraEcho()));
        addChupacabraMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Chupacabra Echo");
    }

    @Test
    @DisplayName("Lands count toward descent but instants do not")
    void countsNoncreaturePermanentCards() {
        harness.setGraveyard(player1, List.of(new Swamp(), new Abrade()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChupacabraEcho());

        castChupacabra(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Descent is evaluated at resolution and then remains fixed")
    void usesGraveyardAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChupacabraEcho());
        harness.setHand(player1, List.of(new ChupacabraEcho()));
        addChupacabraMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new Swamp()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.setGraveyard(player1, List.of(new Swamp(), new Swamp()));
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Chupacabra Echo");
    }

    @Test
    @DisplayName("A creature with zero toughness is put into its owner's graveyard")
    void lethalDescentPutsTargetInGraveyard() {
        harness.setGraveyard(player1, List.of(new Swamp(), new Swamp()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChupacabraEcho());

        castChupacabra(target);

        harness.assertNotOnBattlefield(player2, "Chupacabra Echo");
        harness.assertInGraveyard(player2, "Chupacabra Echo");
        harness.assertOnBattlefield(player1, "Chupacabra Echo");
    }

    @Test
    @DisplayName("The trigger resolves after its source dies and counts that source")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChupacabraEcho());
        harness.setHand(player1, List.of(new ChupacabraEcho()));
        addChupacabraMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Chupacabra Echo"));
        harness.assertInGraveyard(player1, "Chupacabra Echo");
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Chupacabra Echo");
    }

    private void castChupacabra(Permanent target) {
        harness.setHand(player1, List.of(new ChupacabraEcho()));
        addChupacabraMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addChupacabraMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
