package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlossomCladWerewolf;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaverOfBlossoms.class, BlossomCladWerewolf.class})
class WeaverOfBlossomsTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new WeaverOfBlossoms()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void frontFaceAddsOneManaOfChosenColor() {
        Permanent weaver = addReadyFrontFace();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(weaver.isTapped()).isTrue();
    }

    @Test
    void backFaceAddsTwoManaOfOneChosenColor() {
        Permanent werewolf = addReadyBackFace();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(werewolf.isTapped()).isTrue();
    }

    @Test
    void transformsToBackFaceWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        Permanent weaver = addReadyFrontFace();
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(weaver.isTransformed()).isTrue();
        assertThat(weaver.getCard().getName()).isEqualTo("Blossom-Clad Werewolf");
    }

    @Test
    void transformsToFrontFaceWhenNightBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent werewolf = addReadyBackFace();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(werewolf.isTransformed()).isFalse();
        assertThat(werewolf.getCard().getName()).isEqualTo("Weaver of Blossoms");
    }

    @Test
    void entersWithBackFaceUpAtNight() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new WeaverOfBlossoms()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent werewolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(werewolf.isTransformed()).isTrue();
        werewolf.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(werewolf.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remainsDayWhenPreviousActivePlayerCastOneSpell() {
        gd.dayNight = DayNight.DAY;
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        Permanent weaver = addReadyFrontFace();

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(weaver.isTransformed()).isFalse();
    }

    @Test
    void remainsNightWhenOnlyNonactivePlayerCastTwoSpells() {
        gd.dayNight = DayNight.NIGHT;
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        Permanent werewolf = addReadyBackFace();

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(werewolf.isTransformed()).isTrue();
    }
    private Permanent addReadyFrontFace() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new WeaverOfBlossoms());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyBackFace() {
        WeaverOfBlossoms card = new WeaverOfBlossoms();
        Permanent perm = harness.addToBattlefieldAndReturn(player1, card);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        perm.setSummoningSick(false);
        return perm;
    }
}
