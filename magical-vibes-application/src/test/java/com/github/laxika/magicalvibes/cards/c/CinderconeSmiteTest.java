package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderconeSmite.class, HillGiant.class})
class CinderconeSmiteTest extends BaseCardTest {

    @Test
    void startingPlayerDealsDamageWithoutCreatingTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void nonStartingPlayerDealsDamageAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast(player2, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    private void cast(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        prepareCast(caster);
        harness.castAndResolveSorcery(caster, 0, target.getId());
    }

    @Test
    void missingTargetPreventsTreasureCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        prepareCast(player2);
        harness.castSorcery(player2, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player2, "Cindercone Smite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalDamageStillCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        target.setMarkedDamage(1);

        cast(player2, target);

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @Test
    void createdTreasureCanBeSacrificedForManaImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        cast(player2, target);
        Permanent treasure = findPermanents(player2, "Treasure").getFirst();
        assertThat(treasure.isTapped()).isFalse();

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player2, "BLUE");

        harness.assertNotOnBattlefield(player2, "Treasure");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void prepareCast(com.github.laxika.magicalvibes.model.Player caster) {
        harness.forceActivePlayer(caster);
        gd.startingPlayerId = player1.getId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new CinderconeSmite()));
        harness.addMana(caster, ManaColor.RED, 1);
    }
}
