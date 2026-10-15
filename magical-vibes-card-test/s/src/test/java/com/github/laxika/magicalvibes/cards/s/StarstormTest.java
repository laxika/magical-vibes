package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.k.KrosanColossus;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Starstorm.class, ElvishWarrior.class, KrosanColossus.class})
class StarstormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each creature")
    void dealsXDamageToEachCreature() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent colossus = harness.addToBattlefieldAndReturn(player2, new KrosanColossus());
        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player2, "Krosan Colossus");
        assertThat(colossus.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void zeroXDealsNoDamage() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Elvish Warrior");
        assertThat(warrior.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Starstorm()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Starstorm");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cycling discards immediately, draws on resolution, and deals no damage")
    void cyclingPaysDiscardBeforeDrawingAndDoesNotDamageCreatures() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new Starstorm()));
        harness.setLibrary(player1, List.of(new KrosanColossus()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Starstorm");
        harness.assertInGraveyard(player1, "Starstorm");
        harness.assertNotInHand(player1, "Krosan Colossus");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Krosan Colossus");
        harness.assertOnBattlefield(player2, "Elvish Warrior");
        assertThat(warrior.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only two mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new Starstorm()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Starstorm");
        harness.assertNotInGraveyard(player1, "Starstorm");
        harness.assertNotInHand(player1, "Elvish Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals lethal damage to a face-down creature")
    void damagesFaceDownCreatures() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player2, new KrosanColossus());
        colossus.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Krosan Colossus");
    }
}
