package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CitanulStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature and adds one chosen-color mana")
    void tapsCreatureAndAddsMana() {
        harness.addToBattlefield(player1, new CitanulStalwart());
        Permanent stalwart = gd.playerBattlefields.get(player1.getId()).get(0);
        stalwart.setSummoningSick(false);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(stalwart.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped artifact and adds one chosen-color mana")
    void tapsArtifactAndAddsMana() {
        harness.addToBattlefield(player1, new CitanulStalwart());
        Permanent stalwart = gd.playerBattlefields.get(player1.getId()).get(0);
        stalwart.setSummoningSick(false);
        harness.addToBattlefield(player1, new MoxOpal());
        Permanent mox = gd.playerBattlefields.get(player1.getId()).get(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(stalwart.isTapped()).isTrue();
        assertThat(mox.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without another untapped artifact or creature")
    void cannotActivateWithoutAnotherArtifactOrCreature() {
        harness.addToBattlefield(player1, new CitanulStalwart());
        Permanent stalwart = gd.playerBattlefields.get(player1.getId()).get(0);
        stalwart.setSummoningSick(false);
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
