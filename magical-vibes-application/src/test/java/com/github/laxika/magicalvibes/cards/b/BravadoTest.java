package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BravadoTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each other creature you control")
    void boostsPerOtherCreatureYouControl() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        Permanent third = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);
        gd.playerBattlefields.get(player1.getId()).add(third);

        Permanent aura = new Permanent(new Bravado());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts creatures controlled by the Aura controller on an opponent's creature")
    void countsAuraControllersCreatures() {
        Permanent opponentHost = new Permanent(new GrizzlyBears());
        Permanent ownA = new Permanent(new GrizzlyBears());
        Permanent ownB = new Permanent(new GrizzlyBears());
        Permanent opponentOther = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentHost);
        gd.playerBattlefields.get(player2.getId()).add(opponentOther);
        gd.playerBattlefields.get(player1.getId()).add(ownA);
        gd.playerBattlefields.get(player1.getId()).add(ownB);

        Permanent aura = new Permanent(new Bravado());
        aura.setAttachedTo(opponentHost.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, opponentHost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentHost)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentOther)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost changes as other creatures enter and leave")
    void updatesDynamically() {
        Permanent host = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);

        Permanent aura = new Permanent(new Bravado());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);

        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(other);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost ends when Bravado leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);

        Permanent aura = new Permanent(new Bravado());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Bravado()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
