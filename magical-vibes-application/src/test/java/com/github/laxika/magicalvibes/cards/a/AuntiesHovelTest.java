package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CaterwaulingBoggart;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuntiesHovel.class, Forest.class, CaterwaulingBoggart.class,
        AmoeboidChangeling.class, Tarfire.class})
class AuntiesHovelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Goblin card in hand")
    void entersTappedWithoutGoblin() {
        harness.setHand(player1, List.of(new AuntiesHovel(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Goblin lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new AuntiesHovel(), new CaterwaulingBoggart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Goblin in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new AuntiesHovel(), new CaterwaulingBoggart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A changeling in hand can be revealed as a Goblin")
    void entersUntappedWhenRevealingChangeling() {
        AmoeboidChangeling changeling = new AmoeboidChangeling();
        harness.setHand(player1, List.of(new AuntiesHovel(), changeling));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeling);
    }

    @Test
    @DisplayName("A noncreature Goblin card can be revealed and stays in hand")
    void entersUntappedWhenRevealingKindredInstant() {
        Tarfire tarfire = new Tarfire();
        harness.setHand(player1, List.of(new AuntiesHovel(), tarfire));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tarfire);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Goblin card cannot be revealed")
    void entersTappedWhenOnlyOpponentHasGoblin() {
        harness.setHand(player1, List.of(new AuntiesHovel()));
        harness.setHand(player2, List.of(new CaterwaulingBoggart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    private Permanent addLandReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AuntiesHovel());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Auntie's Hovel");
    }
}
