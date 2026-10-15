package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({VaultSkyward.class, CopperMyr.class, DarksteelAxe.class})
class VaultSkywardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vault Skyward grants flying and untaps target creature")
    void grantsFlightAndUntaps() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying granted by Vault Skyward expires at end of turn")
    void flyingExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        Permanent ownCreature = addTappedCreature(player1);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1); // valid target so spell is playable
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelAxe());
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Vault Skyward fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("An untapped creature can gain flying without affecting other creatures")
    void untappedCreatureGainsFlyingAndOtherCreaturesAreUnaffected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        Permanent other = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(other.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CopperMyr());
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }
}
