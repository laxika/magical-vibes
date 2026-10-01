package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.s.SilkbindFaerie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DroveOfElves.class, DevotedDruid.class, SafeholdElite.class, SilkbindFaerie.class,
        ManaReflection.class})
class DroveOfElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a green permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent drove = addDrove(player1);

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of green permanents you control")
    void ptEqualsGreenPermanentCount() {
        Permanent drove = addDrove(player1);
        addCreatureReady(player1, new DevotedDruid());
        addCreatureReady(player1, new SafeholdElite());

        // itself + Devoted Druid + Safehold Elite = 3
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green permanents you control are not counted")
    void ignoresNonGreenPermanents() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new SilkbindFaerie()); // blue and white

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only your green permanents, not the opponent's")
    void ignoresOpponentGreenPermanents() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player2, new DevotedDruid());

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when green permanents change")
    void ptUpdatesWhenGreenPermanentsChange() {
        Permanent drove = addDrove(player1);
        Permanent devotedDruid = addCreatureReady(player1, new DevotedDruid());
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(devotedDruid);
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a green noncreature permanent")
    void countsGreenNoncreaturePermanent() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new ManaReflection());

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(2);
    }

    private Permanent addDrove(Player player) {
        return addCreatureReady(player, new DroveOfElves());
    }
}
