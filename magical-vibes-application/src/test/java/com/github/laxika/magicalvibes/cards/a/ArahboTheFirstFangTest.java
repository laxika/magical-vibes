package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProwlingCaracal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArahboTheFirstFangTest extends BaseCardTest {

    @Test
    @DisplayName("Other Cats you control get +1/+1")
    void buffsOtherCatsYouControl() {
        harness.addToBattlefield(player1, new ArahboTheFirstFang());
        harness.addToBattlefield(player1, new ProwlingCaracal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent arahbo = findPermanent(player1, "Arahbo, the First Fang");
        Permanent cat = findPermanent(player1, "Prowling Caracal");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, arahbo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, arahbo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Arahbo entering creates a Cat token")
    void ownEntryCreatesCatToken() {
        harness.setHand(player1, List.of(new ArahboTheFirstFang()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another nontoken Cat entering creates a Cat token")
    void anotherNontokenCatEntryCreatesCatToken() {
        harness.addToBattlefield(player1, new ArahboTheFirstFang());
        harness.setHand(player1, List.of(new ProwlingCaracal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Cats and Cat tokens do not trigger the ability")
    void nonCatsAndCatTokensDoNotTrigger() {
        harness.setHand(player1, List.of(new ArahboTheFirstFang()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
