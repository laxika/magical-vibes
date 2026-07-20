package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VizierOfTumblingSandsTest extends BaseCardTest {

    // ===== {T}: Untap another target permanent =====

    @Test
    @DisplayName("Untaps a tapped target permanent")
    void untapsTargetPermanent() {
        addReadyVizier(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the untap ability taps Vizier as its cost")
    void activatingTapsVizier() {
        Permanent vizier = addReadyVizier(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(vizier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot untap itself — target must be another permanent")
    void cannotTargetItself() {
        Permanent vizier = addReadyVizier(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vizier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent");
    }

    // ===== Cycling: when you cycle, untap target permanent, then draw =====

    @Test
    @DisplayName("Cycling untaps the target permanent and draws a card")
    void cyclingUntapsTargetAndDraws() {
        harness.setHand(player1, List.of(new VizierOfTumblingSands()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new Forest());
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd2 = harness.getGameData();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd2.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vizier of Tumbling Sands"));
        assertThat(gd2.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private Permanent addReadyVizier(Player player) {
        Permanent perm = new Permanent(new VizierOfTumblingSands());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
