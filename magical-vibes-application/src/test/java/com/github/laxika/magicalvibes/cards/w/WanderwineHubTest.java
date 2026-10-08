package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SilvergillAdept;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
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

@CardUsed({WanderwineHub.class, Forest.class, SilvergillAdept.class,
        MerrowCommerce.class, AmoeboidChangeling.class})
class WanderwineHubTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Merfolk card in hand")
    void entersTappedWithoutMerfolk() {
        harness.setHand(player1, List.of(new WanderwineHub(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Merfolk lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new WanderwineHub(), new SilvergillAdept()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Merfolk in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new WanderwineHub(), new SilvergillAdept()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new WanderwineHub());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new WanderwineHub());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature Merfolk card can be revealed and remains in hand")
    void revealsNoncreatureMerfolk() {
        MerrowCommerce merfolk = new MerrowCommerce();
        harness.setHand(player1, List.of(new WanderwineHub(), merfolk));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(merfolk);
    }

    @Test
    @DisplayName("A changeling in hand can be revealed as a Merfolk")
    void revealsChangeling() {
        AmoeboidChangeling changeling = new AmoeboidChangeling();
        harness.setHand(player1, List.of(new WanderwineHub(), changeling));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeling);
    }

    @Test
    @DisplayName("An opponent's Merfolk in hand cannot keep the land untapped")
    void cannotRevealOpponentsMerfolk() {
        harness.setHand(player1, List.of(new WanderwineHub()));
        harness.setHand(player2, List.of(new SilvergillAdept()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Wanderwine Hub");
    }
}
