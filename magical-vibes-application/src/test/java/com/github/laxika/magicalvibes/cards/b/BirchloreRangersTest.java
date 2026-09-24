package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BirchloreRangers.class, ElvishWarrior.class, GlorySeeker.class})
class BirchloreRangersTest extends BaseCardTest {

    @Test
    void tapsTwoElvesAndAddsManaOfChosenColor() {
        Permanent source = addCreatureReady(player1, new BirchloreRangers());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        Permanent nonElf = addCreatureReady(player1, new GlorySeeker());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
        assertThat(elf.isTapped()).isTrue();
        assertThat(nonElf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutTwoUntappedElves() {
        Permanent source = addCreatureReady(player1, new BirchloreRangers());
        addCreatureReady(player1, new GlorySeeker());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    void requiresTwoUntappedElves() {
        Permanent source = addCreatureReady(player1, new BirchloreRangers());
        Permanent tappedElf = addCreatureReady(player1, new ElvishWarrior());
        tappedElf.tap();

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(tappedElf.isTapped()).isTrue();
    }

    @Test
    void canActivateWithSourceAlreadyTapped() {
        Permanent source = addCreatureReady(player1, new BirchloreRangers());
        source.tap();
        Permanent firstElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent secondElf = addCreatureReady(player1, new ElvishWarrior());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
        assertThat(firstElf.isTapped()).isTrue();
        assertThat(secondElf.isTapped()).isTrue();
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new BirchloreRangers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent rangers = findPermanent(player1, "Birchlore Rangers");
        assertThat(rangers.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(rangers));
        harness.passBothPriorities();

        assertThat(rangers.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
