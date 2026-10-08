package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeechingLurker;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(curse.getCard()).isInstanceOf(CurseOfLeeches.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(curse.isTransformed()).isFalse();
    }

    @Test
    void castingEstablishesDayAndEnchantsTargetPlayer() {
        gd.dayNight = DayNight.NEITHER;
        harness.setHand(player1, List.of(new CurseOfLeeches()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent curse = findPermanent(player1, "Curse of Leeches");
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void castingAtNightEntersAsUnattachedLurker() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new CurseOfLeeches()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent lurker = findPermanent(player1, "Leeching Lurker");
        assertThat(lurker.isTransformed()).isTrue();
        assertThat(lurker.getAttachedTo()).isNull();
    }

    @Test
    void doesNotDrainDuringUnenchantedPlayersUpkeep() {
        placeCurse(player1, player2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void enchantingControllerLosesAndGainsOneLife() {
        placeCurse(player1, player1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void staysDayWhenPreviousActivePlayerCastOneSpell() {
        Permanent curse = placeCurse(player1, player2);
        gd.dayNight = DayNight.DAY;
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(curse.isTransformed()).isFalse();
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void lurkerGainsLifeFromCombatDamage() {
        Permanent curse = placeCurse(player1, player2);
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(curse.getAttachedTo()).isNull();
    }

    private Permanent placeCurse(Player controller, Player enchantedPlayer) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new CurseOfLeeches());
        permanent.setAttachedTo(enchantedPlayer.getId());
        return permanent;
    }
}
