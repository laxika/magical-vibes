package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalvageSlasher.class, GrizzlyBears.class, Memnite.class, MindStone.class})
class SalvageSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Base stats with no artifacts in graveyard")
    void baseStatsWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new SalvageSlasher());

        Permanent slasher = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+0 for each artifact card in your graveyard")
    void boostedPerArtifactCardInGraveyard() {
        harness.addToBattlefield(player1, new SalvageSlasher());
        harness.setGraveyard(player1, List.of(new Memnite(), new MindStone()));

        Permanent slasher = gd.playerBattlefields.get(player1.getId()).getFirst();
        // Base 1/1 + 2 artifact cards = 3/1 (power only)
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-artifact cards in graveyard do not boost")
    void nonArtifactCardsDoNotBoost() {
        harness.addToBattlefield(player1, new SalvageSlasher());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Memnite()));

        Permanent slasher = gd.playerBattlefields.get(player1.getId()).getFirst();
        // Only the artifact (Memnite) counts, not Grizzly Bears
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifacts in an opponent's graveyard do not boost")
    void opponentGraveyardArtifactsDoNotBoost() {
        harness.addToBattlefield(player1, new SalvageSlasher());
        harness.setGraveyard(player2, List.of(new Memnite(), new MindStone()));

        Permanent slasher = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost updates as artifact cards enter and leave the graveyard")
    void boostUpdatesWithGraveyardContents() {
        harness.addToBattlefield(player1, new SalvageSlasher());
        harness.addToBattlefield(player1, new SalvageSlasher());
        Permanent slasher = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new SalvageSlasher(), new SalvageSlasher()));
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new SalvageSlasher()));
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(1);
    }
}
