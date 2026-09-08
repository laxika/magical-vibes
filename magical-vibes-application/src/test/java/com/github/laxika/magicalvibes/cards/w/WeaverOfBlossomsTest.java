package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlossomCladWerewolf;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

    private Permanent addReadyFrontFace() {
        Permanent perm = new Permanent(new WeaverOfBlossoms());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBackFace() {
        WeaverOfBlossoms card = new WeaverOfBlossoms();
        Permanent perm = new Permanent(card);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
