package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GodlessShrine;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Defile.class, Swamp.class, AirElemental.class, GodlessShrine.class})
class DefileTest extends BaseCardTest {

    @Test
    @DisplayName("Defile gives a creature -1/-1 for each Swamp its controller controls")
    void debuffsForEachControlledSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castDefile(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Defile counts only Swamps controlled by its controller at resolution")
    void countsOnlyControllerSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Defile()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Defile's debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castDefile(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Defile cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new Defile()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Defile counts nonbasic Swamps and ignores an opponent's Swamps")
    void countsNonbasicSwamps() {
        harness.addToBattlefield(player1, new GodlessShrine());
        harness.addToBattlefield(player2, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castDefile(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Defile puts a creature with zero toughness into its owner's graveyard")
    void killsCreatureWithZeroToughness() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castDefile(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Defile can target its controller's own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castDefile(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Defile's resolved penalty does not change when more Swamps enter")
    void penaltyIsFixedAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castDefile(target);
        harness.addToBattlefield(player1, new Swamp());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    private void castDefile(Permanent target) {
        harness.setHand(player1, List.of(new Defile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
