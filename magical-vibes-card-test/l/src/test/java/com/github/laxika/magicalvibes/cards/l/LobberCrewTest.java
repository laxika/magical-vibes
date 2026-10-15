package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.r.RakdosShredFreak;
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

@CardUsed({LobberCrew.class, CentaurHealer.class, DrudgeBeetle.class, RakdosShredFreak.class})
class LobberCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to each opponent")
    void tapAbilityDamagesEachOpponent() {
        Permanent crew = addReadyCrew(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a multicolored spell untaps Lobber Crew")
    void multicoloredSpellUntapsCrew() {
        Permanent crew = addReadyCrew(player1);
        crew.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CentaurHealer(), "{1}{G}{W}");
        harness.passBothPriorities(); // resolve the untap trigger

        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a monocolored spell does not untap Lobber Crew")
    void monocoloredSpellDoesNotUntapCrew() {
        Permanent crew = addReadyCrew(player1);
        crew.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DrudgeBeetle(), "{1}{G}");

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Lobber Crew"));
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's multicolored spell does not untap the crew")
    void opponentMulticoloredSpellDoesNotUntapCrew() {
        Permanent crew = addReadyCrew(player1);
        crew.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new CentaurHealer(), "{1}{G}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each crew untaps from its own trigger before the multicolored spell resolves")
    void multipleCrewsUntapIndependently() {
        Permanent first = addReadyCrew(player1);
        Permanent second = addReadyCrew(player1);
        first.tap();
        second.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new CentaurHealer(), "{1}{G}{W}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(first.isTapped()).isNotEqualTo(second.isTapped());
        harness.passBothPriorities();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A hybrid spell is multicolored even when paid for with only red mana")
    void hybridSpellUntapsCrew() {
        Permanent crew = addReadyCrew(player1);
        crew.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RakdosShredFreak()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A tapped crew cannot pay the tap cost again")
    void tappedCrewCannotActivate() {
        Permanent crew = addReadyCrew(player1);
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents the tap ability")
    void summoningSickCrewCannotActivate() {
        harness.addToBattlefield(player1, new LobberCrew());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCrew(Player player) {
        return addCreatureReady(player, new LobberCrew());
    }
}
