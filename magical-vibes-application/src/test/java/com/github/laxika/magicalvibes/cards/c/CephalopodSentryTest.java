package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CephalopodSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts you control; toughness stays 5")
    void powerEqualsControlledArtifacts() {
        Permanent sentry = addSentry(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts only artifacts controlled by the sentry's controller")
    void countsOnlyControllersArtifacts() {
        Permanent sentry = addSentry(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power updates when controlled artifacts enter and leave the battlefield")
    void powerUpdatesWhenArtifactsChange() {
        Permanent sentry = addSentry(player1);

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);

        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Ornithopter);
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
    }

    private Permanent addSentry(Player player) {
        Permanent permanent = new Permanent(new CephalopodSentry());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
