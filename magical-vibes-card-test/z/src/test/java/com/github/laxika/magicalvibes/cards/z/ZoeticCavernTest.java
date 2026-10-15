package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ZoeticCavern.class)
class ZoeticCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new ZoeticCavern());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up as a land")
    void morphsFaceDownAndRestoresLandCharacteristics() {
        harness.setHand(player1, List.of(new ZoeticCavern()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent cavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(cavern.isFaceDown()).isTrue();
        assertThat(gqs.isCreature(gd, cavern)).isTrue();
        assertThat(gqs.isLand(gd, cavern)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cavern));

        assertThat(cavern.isFaceDown()).isFalse();
        assertThat(gqs.isCreature(gd, cavern)).isFalse();
        assertThat(gqs.isLand(gd, cavern)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting face down does not consume a land play")
    void morphDoesNotConsumeLandPlay() {
        harness.setHand(player1, List.of(new ZoeticCavern(), new ZoeticCavern()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent faceDownCavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(faceDownCavern.isFaceDown()).isTrue();
        assertThat(faceDownCavern.getEffectivePower()).isEqualTo(2);
        assertThat(faceDownCavern.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Turning face up preserves tapped state and does not use a land play or the stack")
    void turningFaceUpPreservesTappedState() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new ZoeticCavern());
        cavern.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        cavern.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.turnFaceUp(player1, 0);

        assertThat(cavern.isFaceDown()).isFalse();
        assertThat(cavern.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, cavern)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cavern);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot turn face up without paying two mana")
    void cannotTurnFaceUpWithOnlyOneMana() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new ZoeticCavern());
        cavern.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cavern.isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
