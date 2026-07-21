package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.CompellingArgument;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VileManifestationTest extends BaseCardTest {

    @Test
    @DisplayName("No power boost when no cycling cards in graveyard")
    void noBoostWithoutCyclingCards() {
        Permanent vile = addVileManifestation(player1);

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, vile)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each card with cycling in your graveyard")
    void boostsPerCyclingCard() {
        Permanent vile = addVileManifestation(player1);
        harness.setGraveyard(player1, List.of(new Censor(), new CompellingArgument()));

        // Two cycling cards -> +2/+0; toughness unaffected.
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vile)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-cycling cards in graveyard do not count")
    void ignoresNonCyclingCards() {
        Permanent vile = addVileManifestation(player1);
        harness.setGraveyard(player1, List.of(new Censor(), new GrizzlyBears()));

        // Only the Censor has cycling.
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling cards in opponent's graveyard do not count")
    void ignoresOpponentGraveyard() {
        Permanent vile = addVileManifestation(player1);
        harness.setGraveyard(player2, List.of(new Censor(), new CompellingArgument()));

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(0);
    }

    @Test
    @DisplayName("Power updates dynamically as cycling cards enter the graveyard")
    void updatesDynamically() {
        Permanent vile = addVileManifestation(player1);

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(0);

        harness.setGraveyard(player1, List.of(new Censor()));
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Censor(), new CompellingArgument()));
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addVileManifestation(Player player) {
        Permanent permanent = new Permanent(new VileManifestation());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
