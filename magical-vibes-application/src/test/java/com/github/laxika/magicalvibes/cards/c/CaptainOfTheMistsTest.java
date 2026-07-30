package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainOfTheMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps itself when another Human you control enters")
    void untapsWhenHumanEnters() {
        Permanent captain = addReadyCaptain(player1);
        captain.tap();

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell (triggers Captain)
        harness.passBothPriorities(); // resolve Captain's untap trigger

        assertThat(captain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap when a non-Human creature enters")
    void doesNotUntapForNonHuman() {
        Permanent captain = addReadyCaptain(player1);
        captain.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(captain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap when an opponent's Human enters")
    void doesNotUntapForOpponentHuman() {
        Permanent captain = addReadyCaptain(player1);
        captain.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(captain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability taps an untapped target permanent")
    void abilityTapsUntappedPermanent() {
        addReadyCaptain(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability untaps a tapped target permanent")
    void abilityUntapsTappedPermanent() {
        addReadyCaptain(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability taps the Captain, and a Human entering untaps it again")
    void humanEnterUntapsAfterActivation() {
        Permanent captain = addReadyCaptain(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve Captain's untap trigger

        assertThat(captain.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    private Permanent addReadyCaptain(Player player) {
        Permanent permanent = new Permanent(new CaptainOfTheMists());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
