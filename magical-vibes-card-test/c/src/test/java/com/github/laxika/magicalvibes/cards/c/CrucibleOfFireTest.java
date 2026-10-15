package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlameblastDragon;
import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrucibleOfFire.class, FlameblastDragon.class, JungleWeaver.class,
        MaskwoodNexus.class, Opalescence.class})
class CrucibleOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Dragon creatures you control get +3/+3")
    void buffsOwnDragons() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new FlameblastDragon());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not buff non-Dragon creatures")
    void doesNotBuffNonDragons() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new JungleWeaver());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not buff Dragons controlled by an opponent")
    void doesNotBuffOpponentDragons() {
        harness.addToBattlefield(player1, new CrucibleOfFire());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new FlameblastDragon());

        assertThat(gqs.getEffectivePower(gd, opponentDragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentDragon)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bonus is removed when Crucible of Fire leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new FlameblastDragon());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Crucible of Fire"));

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
    }

    @Test
    @DisplayName("Dragons entering after Crucible of Fire receive the bonus")
    void buffsDragonsEnteringLater() {
        harness.addToBattlefield(player1, new CrucibleOfFire());
        Permanent dragon = harness.enterBattlefieldAndReturn(player1, new FlameblastDragon());

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(8);
    }

    @Test
    @DisplayName("Each Crucible of Fire contributes its own bonus")
    void multipleCopiesStack() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new FlameblastDragon());
        Permanent firstCrucible = harness.addToBattlefieldAndReturn(player1, new CrucibleOfFire());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(11);

        gd.playerBattlefields.get(player1.getId()).remove(firstCrucible);

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(8);
    }

    @Test
    @CardUsed({CrucibleOfFire.class, MaskwoodNexus.class, Opalescence.class})
    @DisplayName("Crucible of Fire buffs itself when it is a Dragon creature")
    void buffsItselfWhenAnimatedAsDragon() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent crucible = harness.addToBattlefieldAndReturn(player1, new CrucibleOfFire());

        assertThat(gqs.getEffectivePower(gd, crucible)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, crucible)).isEqualTo(7);
    }
}
