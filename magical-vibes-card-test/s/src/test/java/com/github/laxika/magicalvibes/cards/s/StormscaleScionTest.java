package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.cards.f.FangkeepersFamiliar;
import com.github.laxika.magicalvibes.cards.t.TwinmawStormbrood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormscaleScion.class, TwinmawStormbrood.class, DalkovanPackbeasts.class,
        FangkeepersFamiliar.class})
class StormscaleScionTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dragons you control get +1/+1")
    void boostsOtherDragonsYouControl() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new TwinmawStormbrood());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        Permanent opposingDragon = harness.addToBattlefieldAndReturn(player2, new TwinmawStormbrood());
        int dragonPower = gqs.getEffectivePower(gd, dragon);
        int dragonToughness = gqs.getEffectiveToughness(gd, dragon);
        int nonDragonPower = gqs.getEffectivePower(gd, nonDragon);
        int nonDragonToughness = gqs.getEffectiveToughness(gd, nonDragon);
        int opposingDragonPower = gqs.getEffectivePower(gd, opposingDragon);
        int opposingDragonToughness = gqs.getEffectiveToughness(gd, opposingDragon);
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new StormscaleScion());

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(dragonPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(dragonToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonDragon)).isEqualTo(nonDragonPower);
        assertThat(gqs.getEffectiveToughness(gd, nonDragon)).isEqualTo(nonDragonToughness);
        assertThat(gqs.getEffectivePower(gd, opposingDragon)).isEqualTo(opposingDragonPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingDragon)).isEqualTo(opposingDragonToughness);
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(scion.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(scion.getCard().getToughness());
    }

    @Test
    @DisplayName("Storm creates token copies for each spell cast before it this turn")
    void stormCreatesTokenCopies() {
        gd.recordSpellCast(player1.getId(), new DalkovanPackbeasts());
        gd.recordSpellCast(player2.getId(), new DalkovanPackbeasts());
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

    @Test
    @DisplayName("Entering without being cast does not trigger storm")
    void enteringWithoutCastingDoesNotTriggerStorm() {
        gd.recordSpellCast(player1.getId(), new DalkovanPackbeasts());

        harness.enterBattlefieldAndReturn(player1, new StormscaleScion());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A later storm spell counts previous casts but not their copies")
    void laterStormDoesNotCountCopies() {
        harness.castFromHand(player1, new DalkovanPackbeasts(), "{2}{W}");
        harness.passBothPriorities();
        castScion();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        castScion();
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Countering the original before storm resolves does not stop its copies")
    void stormSurvivesCounteringOriginal() {
        gd.recordSpellCast(player1.getId(), new DalkovanPackbeasts());
        StormscaleScion original = new StormscaleScion();
        harness.castFromHand(player1, original, "{4}{R}{R}");
        harness.setHand(player2, List.of(new FangkeepersFamiliar()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0, 2, original.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, original.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Stormscale Scion");

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The original and storm tokens each boost the other Dragons")
    void stormTokensRetainTheirDragonBoost() {
        harness.castFromHand(player1, new DalkovanPackbeasts(), "{2}{W}");
        harness.passBothPriorities();
        castScion();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> scions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stormscale Scion"))
                .toList();
        assertThat(scions).hasSize(2).allSatisfy(scion -> {
            assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(scion.getCard().getPower() + 1);
            assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(scion.getCard().getToughness() + 1);
        });
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
    }
    private void castScion() {
        harness.castFromHand(player1, new StormscaleScion(), "{4}{R}{R}");
    }
}
