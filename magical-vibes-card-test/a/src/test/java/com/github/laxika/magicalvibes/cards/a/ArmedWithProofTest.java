package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmedWithProof.class, GrizzlyBears.class, ValMaroonedSurveyor.class})
class ArmedWithProofTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates twice when it enters the battlefield")
    void investigatesTwiceWhenItEnters() {
        castArmedWithProof();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Clues you control become Equipment and can equip creatures")
    void cluesBecomeEquipmentAndCanEquipCreatures() {
        castArmedWithProof();
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(clue.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void castArmedWithProof() {
        harness.castFromHand(player1, new ArmedWithProof(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void investigatesAsTwoSeparateActions() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        castArmedWithProof();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 4);
    }

    @Test
    void multipleEquippedCluesStackTheirBonuses() {
        castArmedWithProof();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (Permanent clue : findPermanents(player1, "Clue")) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue),
                    1, null, creature.getId());
            harness.passBothPriorities();
            assertThat(clue.getAttachedTo()).isEqualTo(creature.getId());
        }

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equippedClueRetainsItsSacrificeAndDrawAbility() {
        castArmedWithProof();
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue),
                1, null, creature.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue),
                0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clue);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void onlyCluesCurrentlyControlledByTheEnchantmentControllerBecomeEquipment() {
        castArmedWithProof();
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(clue);
        gd.playerBattlefields.get(player2.getId()).add(clue);

        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.CLUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.EQUIPMENT)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(clue);
        gd.playerBattlefields.get(player1.getId()).add(clue);

        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.EQUIPMENT)).isTrue();
    }
}
