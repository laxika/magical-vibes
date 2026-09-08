package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrecipitousDrop.class, GrizzlyBears.class})
class PrecipitousDropTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature and makes its controller venture")
    void entersAndVentures() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setPower(4);
        creatureCard.setToughness(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        harness.setHand(player1, List.of(new PrecipitousDrop()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives the enchanted creature -2/-2 before dungeon completion")
    void givesBasePenalty() {
        Permanent creature = addEnchantedCreature(6, 6);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives the enchanted creature -5/-5 after dungeon completion")
    void givesCompletedDungeonPenalty() {
        Permanent creature = addEnchantedCreature(6, 6);
        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    private Permanent addEnchantedCreature(int power, int toughness) {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setPower(power);
        creatureCard.setToughness(toughness);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new PrecipitousDrop());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return creature;
    }
}
