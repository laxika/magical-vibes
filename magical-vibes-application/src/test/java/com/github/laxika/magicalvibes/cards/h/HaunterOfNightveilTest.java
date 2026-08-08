package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HaunterOfNightveilTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent creatures get -1/-0")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own creatures are unaffected")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not debuff itself")
    void doesNotDebuffItself() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());

        Permanent haunter = findPermanent(player1, "Haunter of Nightveil");

        assertThat(gqs.getEffectivePower(gd, haunter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, haunter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two copies stack to -2/-0")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Penalty applies on resolve and is removed when it leaves")
    void penaltyAppliesOnResolveAndEndsWhenItLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HaunterOfNightveil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Haunter of Nightveil"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }
}
