package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({CaveOfTheFrostDragon.class, Plains.class})
class CaveOfTheFrostDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Cave of the Frost Dragon enters tapped with fewer than two other lands")
    void entersTappedWithFewerThanTwoOtherLands() {
        harness.setHand(player1, List.of(new CaveOfTheFrostDragon()));
        playLand();

        assertThat(findCave().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cave of the Frost Dragon enters untapped with two other lands")
    void entersUntappedWithTwoOtherLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new CaveOfTheFrostDragon()));
        playLand();

        assertThat(findCave().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping Cave of the Frost Dragon produces one white mana")
    void tappingProducesWhiteMana() {
        Permanent cave = addCaveReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(cave.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cave of the Frost Dragon becomes a 3/4 white Dragon creature with flying")
    void animatesAsWhiteDragon() {
        Permanent cave = addCaveReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cave)).isTrue();
        assertThat(gqs.isLand(gd, cave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cave)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cave)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, cave)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, cave)).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, cave, Keyword.FLYING)).isTrue();
        assertThat(cave.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cave of the Frost Dragon's animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent cave = addCaveReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, cave)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cave)).isFalse();
        assertThat(gqs.isLand(gd, cave)).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addCaveReady(Player player) {
        Permanent cave = new Permanent(new CaveOfTheFrostDragon());
        cave.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(cave);
        return cave;
    }

    private Permanent findCave() {
        return findPermanent(player1, "Cave of the Frost Dragon");
    }
}
