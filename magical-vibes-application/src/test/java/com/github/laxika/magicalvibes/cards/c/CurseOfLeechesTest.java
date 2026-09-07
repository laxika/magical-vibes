package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeechingLurker;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfLeeches.class, LeechingLurker.class})
class CurseOfLeechesTest extends BaseCardTest {

    @Test
    void enchantedPlayerLosesLifeAndControllerGainsLifeOnUpkeep() {
        placeCurse(player1, player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void transformsIntoLeechingLurkerAtNight() {
        Permanent curse = placeCurse(player1, player2);
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(curse.getCard()).isInstanceOf(LeechingLurker.class);
        assertThat(curse.isTransformed()).isTrue();
        assertThat(curse.getAttachedTo()).isNull();
    }

    @Test
    void transformsIntoCurseOfLeechesAtDayAndAttachesToChosenPlayer() {
        Permanent curse = placeCurse(player1, player2);
        curse.setCard(curse.getOriginalCard().getBackFaceCard());
        curse.setTransformed(true);
        curse.setAttachedTo(null);
        gd.dayNight = DayNight.NIGHT;
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(curse.getCard()).isInstanceOf(CurseOfLeeches.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(curse.isTransformed()).isFalse();
    }

    private Permanent placeCurse(Player controller, Player enchantedPlayer) {
        Permanent permanent = new Permanent(new CurseOfLeeches());
        permanent.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }
}
