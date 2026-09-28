package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HyperionBlacksmith.class, BronzeHorse.class, DurkwoodBoars.class})
class HyperionBlacksmithTest extends BaseCardTest {

    @Test
    void tappingAbilityTapsBlacksmithAsCost() {
        Permanent blacksmith = addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(blacksmith.isTapped()).isTrue();
    }

    @Test
    void tapsOpponentArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untapsOpponentArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void mayDeclineTappingOrUntappingOpponentArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetOwnArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    void cannotTargetOpponentCreature() {
        addReadyBlacksmith(player1);
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    void targetThatBecomesControlledByBlacksmithControllerIsNoLongerLegal() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyBlacksmith(Player player) {
        return addCreatureReady(player, new HyperionBlacksmith());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BronzeHorse());
    }
}
