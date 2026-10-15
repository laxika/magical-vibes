package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostboilSnarl.class, Island.class, Mountain.class})
class FrostboilSnarlTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Island or Mountain card in hand")
    void entersTappedWithoutIslandOrMountain() {
        harness.setHand(player1, List.of(new FrostboilSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing an Island lets it enter untapped")
    void entersUntappedWhenRevealingIsland() {
        harness.setHand(player1, List.of(new FrostboilSnarl(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Mountain lets it enter untapped")
    void entersUntappedWhenRevealingMountain() {
        harness.setHand(player1, List.of(new FrostboilSnarl(), new Mountain()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new FrostboilSnarl(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new FrostboilSnarl());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        harness.addToBattlefield(player1, new FrostboilSnarl());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Frostboil Snarl in hand cannot be revealed as an Island or Mountain")
    void entersTappedWithOnlyAnotherSnarlInHand() {
        harness.setHand(player1, List.of(new FrostboilSnarl(), new FrostboilSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An Island in an opponent's hand does not let it enter untapped")
    void entersTappedWithIslandInOpponentsHand() {
        harness.setHand(player1, List.of(new FrostboilSnarl()));
        harness.setHand(player2, List.of(new Island()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Island on the battlefield cannot be revealed from hand")
    void entersTappedWithIslandOnlyOnBattlefield() {
        harness.setHand(player1, List.of(new FrostboilSnarl()));
        harness.addToBattlefield(player1, new Island());
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing leaves the same Island card in hand and adds no stack entry")
    void revealingKeepsCardInHand() {
        Island island = new Island();
        harness.setHand(player1, List.of(new FrostboilSnarl(), island));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.stack).isEmpty();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findLand() {
        return findPermanent(player1, "Frostboil Snarl");
    }
}
