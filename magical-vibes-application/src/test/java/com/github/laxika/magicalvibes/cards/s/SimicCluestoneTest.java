package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.m.MazeGlider;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicCluestone.class, MazeGlider.class})
class SimicCluestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Simic Cluestone adds green or blue mana")
    void tappingAddsChosenMana() {
        Permanent cluestone = addReadyCluestone();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(cluestone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying green and blue mana sacrifices Simic Cluestone and draws a card")
    void payingGreenAndBlueSacrificesAndDraws() {
        Permanent cluestone = addReadyCluestone();
        harness.setLibrary(player1, List.of(new MazeGlider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cluestone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof MazeGlider);
    }

    @Test
    @DisplayName("A newly entered noncreature Cluestone can produce green mana")
    void newlyEnteredCluestoneAddsGreenMana() {
        Permanent cluestone = harness.addToBattlefieldAndReturn(player1, new SimicCluestone());
        cluestone.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(cluestone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Cluestone cannot activate either ability")
    void tappedCluestoneCannotActivate() {
        Permanent cluestone = addReadyCluestone();
        cluestone.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cluestone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two green mana cannot pay the green and blue draw cost")
    void drawAbilityRequiresBothColors() {
        Permanent cluestone = addReadyCluestone();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cluestone.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cluestone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered Cluestone is sacrificed immediately but draws only on resolution")
    void sacrificeIsACostAndDrawUsesTheStack() {
        Permanent cluestone = harness.addToBattlefieldAndReturn(player1, new SimicCluestone());
        cluestone.setSummoningSick(true);
        MazeGlider drawnCard = new MazeGlider();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cluestone.getCard());
        assertThat(cluestone.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCluestone() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new SimicCluestone());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
