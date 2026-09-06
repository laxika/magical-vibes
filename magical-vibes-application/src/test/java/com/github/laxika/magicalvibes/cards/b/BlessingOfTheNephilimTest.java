package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessingOfTheNephilim.class, GrizzlyBears.class, QasaliAmbusher.class, Mountain.class})
class BlessingOfTheNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each of its colors")
    void boostsByColorCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());

        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("A monocolored enchanted creature gets +1/+1")
    void boostsMonocoloredCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Blessing of the Nephilim leaves the battlefield")
    void bonusStopsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());

        castBlessing(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Blessing of the Nephilim"));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BlessingOfTheNephilim()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castBlessing(Permanent creature) {
        harness.setHand(player1, List.of(new BlessingOfTheNephilim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
