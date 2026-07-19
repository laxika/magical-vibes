package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NacatlSavageTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent savage = new Permanent(new NacatlSavage());
        savage.setSummoningSick(false);
        savage.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(savage);

        Permanent ironMyr = new Permanent(new IronMyr());
        ironMyr.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ironMyr);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent savage = new Permanent(new NacatlSavage());
        savage.setSummoningSick(false);
        savage.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(savage);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        harness.addToBattlefield(player1, new NacatlSavage());

        Permanent savage = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nacatl Savage"))
                .findFirst().orElseThrow();

        savage.resetModifiers();

        Permanent artifactSource = new Permanent(new IronMyr());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, savage, artifactSource)).isTrue();

        Permanent nonArtifactSource = new Permanent(new GrizzlyBears());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, savage, nonArtifactSource)).isFalse();
    }
}
