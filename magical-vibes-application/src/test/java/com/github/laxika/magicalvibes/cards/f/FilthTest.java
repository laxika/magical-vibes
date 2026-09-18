package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Filth.class, Forest.class, GrizzlyBears.class, Swamp.class})
class FilthTest extends BaseCardTest {

    @Test
    @DisplayName("A Filth in the graveyard gives your creatures swampwalk while you control a Swamp")
    void grantsSwampwalkFromGraveyardWithSwamp() {
        harness.setGraveyard(player1, List.of(new Filth()));
        harness.addToBattlefield(player1, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Filth's graveyard ability turns off without a Swamp or after Filth leaves the graveyard")
    void graveyardAbilityTurnsOffWhenConditionChanges() {
        Filth filth = new Filth();
        harness.setGraveyard(player1, List.of(filth));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();

        Swamp swamp = new Swamp();
        harness.addToBattlefield(player1, swamp);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == swamp);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Filth's intrinsic swampwalk prevents blocking when the defender controls a Swamp")
    void intrinsicSwampwalkPreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new Filth());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Swampwalk granted by Filth's graveyard ability prevents blocking")
    void grantedSwampwalkPreventsBlocking() {
        harness.setGraveyard(player1, List.of(new Filth()));
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A non-Swamp land does not enable Filth's graveyard ability")
    void nonSwampLandDoesNotEnableGraveyardAbility() {
        harness.setGraveyard(player1, List.of(new Filth()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    @DisplayName("Filth requires its graveyard controller to control a Swamp")
    void requiresSwampControlledByFilthController() {
        harness.setGraveyard(player2, List.of(new Filth()));
        harness.addToBattlefield(player1, new Swamp());
        Permanent playerTwoBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, playerTwoBears, Keyword.SWAMPWALK)).isFalse();

        harness.addToBattlefield(player2, new Swamp());
        assertThat(gqs.hasKeyword(gd, playerTwoBears, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("Filth's own swampwalk prevents blocking while the defending player controls a Swamp")
    void ownSwampwalkPreventsBlockingWithSwamp() {
        Permanent attacker = addCreatureReady(player1, new Filth());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Filth's own swampwalk allows blocking while the defending player controls no Swamp")
    void ownSwampwalkAllowsBlockingWithoutSwamp() {
        Permanent attacker = addCreatureReady(player1, new Filth());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }
}
