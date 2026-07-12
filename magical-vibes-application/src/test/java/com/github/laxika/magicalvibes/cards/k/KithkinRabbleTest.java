package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KithkinRabbleTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a white permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent rabble = addRabble(player1);

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of white permanents you control")
    void ptEqualsWhitePermanents() {
        Permanent rabble = addRabble(player1);
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new EliteVanguard());

        // itself + 2 white creatures = 3
        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-white permanents are not counted")
    void nonWhiteNotCounted() {
        Permanent rabble = addRabble(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your white permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent rabble = addRabble(player1);
        harness.addToBattlefield(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when white permanents change")
    void ptUpdatesWhenWhitePermanentsChange() {
        Permanent rabble = addRabble(player1);
        harness.addToBattlefield(player1, new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    private Permanent addRabble(Player player) {
        Permanent permanent = new Permanent(new KithkinRabble());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
