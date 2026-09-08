package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgothianPixiesTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent pixies = addReady(player1, new ArgothianPixies());
        pixies.setAttacking(true);
        Permanent ornithopter = addReady(player2, new Ornithopter());

        preparePixiesDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(ornithopter),
                        gd.playerBattlefields.get(player1.getId()).indexOf(pixies)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents combat damage from artifact creatures")
    void preventsCombatDamageFromArtifactCreatures() {
        Permanent pixies = addReady(player1, new ArgothianPixies());
        Permanent goldMyr = addReady(player2, new GoldMyr());
        goldMyr.setAttacking(true);

        preparePixiesDeclareBlockers();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pixies);
        assertThat(pixies.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from nonartifact creatures")
    void doesNotPreventCombatDamageFromNonartifactCreatures() {
        addReady(player1, new ArgothianPixies());
        Permanent elves = addReady(player2, new LlanowarElves());
        elves.setAttacking(true);

        preparePixiesDeclareBlockers();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Argothian Pixies");
    }

    @Test
    @DisplayName("Prevents noncombat damage from artifact creatures")
    void preventsNoncombatDamageFromArtifactCreatures() {
        Permanent pixies = addReady(player1, new ArgothianPixies());
        Permanent triskelion = addReady(player2, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        UUID pixiesId = pixies.getId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, pixiesId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pixies);
        assertThat(pixies.getMarkedDamage()).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void preparePixiesDeclareBlockers() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
