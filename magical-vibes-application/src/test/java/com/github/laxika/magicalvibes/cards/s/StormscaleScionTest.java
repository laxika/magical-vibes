package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormscaleScion.class, DragonWhelp.class, GrizzlyBears.class})
class StormscaleScionTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dragons you control get +1/+1")
    void boostsOtherDragonsYouControl() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingDragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        int dragonPower = gqs.getEffectivePower(gd, dragon);
        int dragonToughness = gqs.getEffectiveToughness(gd, dragon);
        int nonDragonPower = gqs.getEffectivePower(gd, nonDragon);
        int nonDragonToughness = gqs.getEffectiveToughness(gd, nonDragon);
        int opposingDragonPower = gqs.getEffectivePower(gd, opposingDragon);
        int opposingDragonToughness = gqs.getEffectiveToughness(gd, opposingDragon);
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new StormscaleScion());
        int scionPower = gqs.getEffectivePower(gd, scion);
        int scionToughness = gqs.getEffectiveToughness(gd, scion);

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(dragonPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(dragonToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonDragon)).isEqualTo(nonDragonPower);
        assertThat(gqs.getEffectiveToughness(gd, nonDragon)).isEqualTo(nonDragonToughness);
        assertThat(gqs.getEffectivePower(gd, opposingDragon)).isEqualTo(opposingDragonPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingDragon)).isEqualTo(opposingDragonToughness);
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(scionPower);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(scionToughness);
    }

    @Test
    @DisplayName("Storm creates token copies for each spell cast before it this turn")
    void stormCreatesTokenCopies() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        castScion();

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> scions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stormscale Scion"))
                .toList();
        assertThat(scions).hasSize(3);
        assertThat(scions.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("Storm creates no copies when it is the first spell of the turn")
    void stormCreatesNoCopiesWithoutPriorSpells() {
        castScion();

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stormscale Scion"))
                .count()).isEqualTo(1);
    }

    private void castScion() {
        harness.setHand(player1, List.of(new StormscaleScion()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
    }
}
