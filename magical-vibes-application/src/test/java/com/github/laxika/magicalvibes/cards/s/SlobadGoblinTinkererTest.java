package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlobadGoblinTinkererTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact grants target artifact indestructible")
    void sacrificeArtifactGrantsIndestructible() {
        addSlobadReady(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        addSlobadReady(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        addSlobadReady(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSlobadReady(Player player) {
        Permanent slobad = new Permanent(new SlobadGoblinTinkerer());
        slobad.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(slobad);
        return slobad;
    }
}
