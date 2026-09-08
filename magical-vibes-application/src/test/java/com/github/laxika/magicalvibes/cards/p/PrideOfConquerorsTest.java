package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrideOfConquerorsTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, creatures you control get +1/+1")
    void boostsOwnCreaturesWithoutBlessing() {
        Permanent creature = addCreatureAndForests(8);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castAndResolve();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With ten permanents, ascend gives the city's blessing before the +2/+2 boost")
    void ascendsAndBoostsByTwo() {
        Permanent creature = addCreatureAndForests(9);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castAndResolve();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("The city's blessing remains after the controller has fewer than ten permanents")
    void blessingPersists() {
        addCreatureAndForests(9);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        castAndResolve();

        gd.playerBattlefields.get(player1.getId()).removeLast();
        gd.playerBattlefields.get(player1.getId()).removeLast();
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        castAndResolve();

        assertThat(newCreature.getEffectivePower()).isEqualTo(4);
        assertThat(newCreature.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addCreatureAndForests(int forestCount) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        for (int i = 0; i < forestCount; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        return creature;
    }

    private void castAndResolve() {
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
