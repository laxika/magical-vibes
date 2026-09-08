package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConclavesBlessing.class, GrizzlyBears.class, FountainOfYouth.class})
class ConclavesBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+2 for each other creature you control")
    void boostsByOtherControlledCreatures() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        Permanent third = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);
        gd.playerBattlefields.get(player1.getId()).add(third);

        Permanent aura = new Permanent(new ConclavesBlessing());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts all creatures controlled by the Aura controller when enchanting an opponent's creature")
    void countsAuraControllersCreatures() {
        Permanent opponentHost = new Permanent(new GrizzlyBears());
        Permanent ownA = new Permanent(new GrizzlyBears());
        Permanent ownB = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentHost);
        gd.playerBattlefields.get(player1.getId()).add(ownA);
        gd.playerBattlefields.get(player1.getId()).add(ownB);

        Permanent aura = new Permanent(new ConclavesBlessing());
        aura.setAttachedTo(opponentHost.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, opponentHost)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHost)).isEqualTo(6);
    }

    @Test
    @DisplayName("Updates the boost when a controlled creature leaves")
    void updatesWhenCreatureLeaves() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);

        Permanent aura = new Permanent(new ConclavesBlessing());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ConclavesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
