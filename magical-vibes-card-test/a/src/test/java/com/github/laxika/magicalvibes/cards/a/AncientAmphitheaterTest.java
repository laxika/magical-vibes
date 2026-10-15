package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientAmphitheater.class, BlindSpotGiant.class, Forest.class,
        AvianChangeling.class, CribSwap.class})
class AncientAmphitheaterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Giant card in hand")
    void entersTappedWithoutGiant() {
        harness.setHand(player1, List.of(new AncientAmphitheater(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Giant lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new AncientAmphitheater(), new BlindSpotGiant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Giant in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new AncientAmphitheater(), new BlindSpotGiant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        harness.addToBattlefield(player1, new AncientAmphitheater());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new AncientAmphitheater());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with changeling can be revealed as a Giant")
    void revealsChangelingCreature() {
        harness.setHand(player1, List.of(new AncientAmphitheater(), new AvianChangeling()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        harness.assertInHand(player1, "Avian Changeling");
    }

    @Test
    @DisplayName("A noncreature card with changeling can be revealed as a Giant")
    void revealsChangelingInstant() {
        harness.setHand(player1, List.of(new AncientAmphitheater(), new CribSwap()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        harness.assertInHand(player1, "Crib Swap");
    }

    @Test
    @DisplayName("An opponent's Giant in hand cannot be revealed")
    void opponentsGiantDoesNotAllowUntappedEntry() {
        harness.setHand(player1, List.of(new AncientAmphitheater()));
        harness.setHand(player2, List.of(new BlindSpotGiant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Giant on the battlefield cannot be revealed from hand")
    void battlefieldGiantDoesNotAllowUntappedEntry() {
        harness.setHand(player1, List.of(new AncientAmphitheater()));
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Ancient Amphitheater");
    }
}
