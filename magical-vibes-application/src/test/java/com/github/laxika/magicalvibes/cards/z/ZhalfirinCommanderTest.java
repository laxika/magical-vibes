package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhalfirinCommander.class, ZhalfirinKnight.class, FemerefScouts.class})
class ZhalfirinCommanderTest extends BaseCardTest {

    private void addCommander() {
        addCreatureReady(player1, new ZhalfirinCommander());
        harness.addMana(player1, ManaColor.WHITE, 3);
    }

    @Test
    @DisplayName("Ability gives target Knight +1/+1 until end of turn")
    void boostsTargetKnight() {
        addCommander();
        addCreatureReady(player1, new ZhalfirinKnight());

        UUID targetId = harness.getPermanentId(player1, "Zhalfirin Knight");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Zhalfirin Knight");
        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCommander();
        addCreatureReady(player1, new ZhalfirinKnight());

        UUID targetId = harness.getPermanentId(player1, "Zhalfirin Knight");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Zhalfirin Knight");
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot target a non-Knight creature")
    void rejectsNonKnightTarget() {
        addCommander();
        addCreatureReady(player1, new FemerefScouts());

        UUID targetId = harness.getPermanentId(player1, "Femeref Scouts");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can target an opponent's Knight creature")
    void boostsOpponentsKnight() {
        addCommander();
        Permanent knight = addCreatureReady(player2, new ZhalfirinKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        commander.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        commander.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }
}
