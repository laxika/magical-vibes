package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScepterOfEmpires;
import com.github.laxika.magicalvibes.cards.t.ThroneOfEmpires;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrownOfEmpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without both partners the ability only taps the target creature")
    void tapsTargetWithoutPartners() {
        addReadyArtifact(player1, new CrownOfEmpires());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("With only one partner the ability still just taps the target creature")
    void tapsTargetWithOnlyOnePartner() {
        addReadyArtifact(player1, new CrownOfEmpires());
        addReadyArtifact(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("With both partners you gain control of the target instead of tapping it")
    void gainsControlWithBothPartners() {
        addReadyArtifact(player1, new CrownOfEmpires());
        addReadyArtifact(player1, new ScepterOfEmpires());
        addReadyArtifact(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyArtifact(player1, new CrownOfEmpires());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyArtifact(player1, new CrownOfEmpires());
        Permanent throne = addReadyArtifact(player2, new ThroneOfEmpires());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, throne.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArtifact(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
