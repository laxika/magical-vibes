package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({LairOfTheHydra.class, Mountain.class})
class LairOfTheHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Lair of the Hydra enters tapped with two other lands")
    void entersTappedWithTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        playLair();

        assertThat(findLair().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lair of the Hydra enters untapped with fewer than two other lands")
    void entersUntappedWithFewerThanTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        playLair();

        assertThat(findLair().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping Lair of the Hydra produces one green mana")
    void tappingProducesGreenMana() {
        Permanent lair = addLairReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(lair.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lair of the Hydra becomes an X/X green Hydra creature and stays a land")
    void animatesAsHydra() {
        Permanent lair = addLairReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, lair)).isTrue();
        assertThat(gqs.isLand(gd, lair)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lair)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lair)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, lair)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, lair)).contains(CardSubtype.HYDRA);
    }

    @Test
    @DisplayName("Lair of the Hydra's animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent lair = addLairReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, lair)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, lair)).isFalse();
        assertThat(gqs.isLand(gd, lair)).isTrue();
    }

    @Test
    @DisplayName("Lair of the Hydra cannot animate with X equal to zero")
    void cannotAnimateWithZeroX() {
        Permanent lair = addLairReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("X must be at least 1");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, lair)).isFalse();
    }

    private void playLair() {
        harness.setHand(player1, List.of(new LairOfTheHydra()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addLairReady(Player player) {
        Permanent lair = new Permanent(new LairOfTheHydra());
        lair.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lair);
        return lair;
    }

    private Permanent findLair() {
        return findPermanent(player1, "Lair of the Hydra");
    }
}
