package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamutVoiceOfDissentTest extends BaseCardTest {

    // ===== Static: other creatures you control have haste =====

    @Test
    @DisplayName("Other creatures you control have haste")
    void grantsHasteToOtherCreatures() {
        harness.addToBattlefield(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to opponent's creatures")
    void doesNotGrantHasteToOpponentCreatures() {
        harness.addToBattlefield(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is removed when Samut leaves the battlefield")
    void hasteRemovedWhenSamutLeaves() {
        harness.addToBattlefield(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Samut, Voice of Dissent"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    // ===== {W}, {T}: Untap another target creature =====

    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTargetCreature() {
        addReadySamut(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = findPermanent(player2, "Grizzly Bears");
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the untap ability taps Samut as its cost")
    void activatingTapsSamut() {
        Permanent samut = addReadySamut(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(samut.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot untap itself — target must be another creature")
    void cannotTargetItself() {
        Permanent samut = addReadySamut(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, samut.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    private Permanent addReadySamut(Player player) {
        Permanent perm = new Permanent(new SamutVoiceOfDissent());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
