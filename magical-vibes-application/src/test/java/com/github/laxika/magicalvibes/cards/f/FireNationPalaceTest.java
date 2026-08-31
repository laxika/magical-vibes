package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireNationPalace.class, Forest.class, GrizzlyBears.class})
class FireNationPalaceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped unless its controller controls a basic land")
    void entersTappedWithoutBasicLand() {
        Permanent palace = playLand(player1, new FireNationPalace());
        assertThat(palace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller controls a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent palace = playLand(player1, new FireNationPalace());
        assertThat(palace.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps to add red mana")
    void tapsForRedMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent palace = harness.addToBattlefieldAndReturn(player1, new FireNationPalace());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(palace.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives a creature firebending four until end of turn")
    void grantsFirebendingFour() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new FireNationPalace());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        declareAttackers(List.of(2));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Can target only a creature its controller controls")
    void targetsOnlyOwnCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new FireNationPalace());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 1, 1, null, opposingCreature.getId()))
                .hasMessageContaining("creature you control");
    }

    private Permanent playLand(Player player, com.github.laxika.magicalvibes.model.Card land) {
        harness.setHand(player, List.of(land));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player, 0);
        return gd.playerBattlefields.get(player.getId()).getLast();
    }
}
