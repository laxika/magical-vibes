package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MossfireEgg.class)
class MossfireEggTest extends BaseCardTest {

    @Test
    void activationSacrificesAddsBothManaAndDraws() {
        harness.addToBattlefield(player1, new MossfireEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Mossfire Egg");
        harness.assertInGraveyard(player1, "Mossfire Egg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresTwoGenericMana() {
        harness.addToBattlefield(player1, new MossfireEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Mossfire Egg");
    }

    @Test
    void activationRequiresAnUntappedSource() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new MossfireEgg());
        egg.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Mossfire Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void activationCanRespondToAnotherAbility() {
        harness.addToBattlefield(player1, new MossfireEgg());
        harness.addToBattlefield(player1, new MossfireEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
}
