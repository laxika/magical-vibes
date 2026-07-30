package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnabaSpiritCrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Minotaur creatures get +1/+0")
    void buffsOtherMinotaurs() {
        harness.addToBattlefield(player1, new AnabaBodyguard());
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());

        Permanent bodyguard = findPermanent(player1, "Anaba Bodyguard");

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bodyguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Anaba Spirit Crafter buffs itself since it is a Minotaur")
    void buffsItself() {
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());

        Permanent crafter = findPermanent(player1, "Anaba Spirit Crafter");

        assertThat(gqs.getEffectivePower(gd, crafter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crafter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Minotaur creatures")
    void doesNotBuffNonMinotaurs() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs opponent's Minotaur creatures too")
    void buffsOpponentMinotaurs() {
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());
        harness.addToBattlefield(player2, new AnabaBodyguard());

        Permanent opponentMinotaur = findPermanent(player2, "Anaba Bodyguard");

        assertThat(gqs.getEffectivePower(gd, opponentMinotaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentMinotaur)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus applies when Anaba Spirit Crafter resolves onto the battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new AnabaBodyguard());
        harness.setHand(player1, List.of(new AnabaSpiritCrafter()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent bodyguard = findPermanent(player1, "Anaba Bodyguard");

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Anaba Spirit Crafter leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());
        harness.addToBattlefield(player1, new AnabaBodyguard());

        Permanent bodyguard = findPermanent(player1, "Anaba Bodyguard");

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Anaba Spirit Crafter"));

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bodyguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two copies stack their bonuses")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());
        harness.addToBattlefield(player1, new AnabaSpiritCrafter());
        harness.addToBattlefield(player1, new AnabaBodyguard());

        Permanent bodyguard = findPermanent(player1, "Anaba Bodyguard");

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bodyguard)).isEqualTo(3);
    }
}
