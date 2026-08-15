package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EliminateTheCompetitionTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void castWithSacrifices(List<UUID> targetIds, List<UUID> sacrificeIds) {
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of(), false, null,
                null, null, null, null, false, null, null, null, sacrificeIds);
    }

    @Test
    @DisplayName("Sacrificing one creature destroys one target creature")
    void sacrificesAndDestroysMatchingX() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(target.getId()), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing no creatures destroys no targets")
    void zeroDoesNothing() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature to set X")
    void cannotSacrificeNoncreature() {
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(land);
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(land.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }
}
