package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningAxe.class, AshcoatBear.class, HavenwoodWurm.class})
class LightningAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and deals 5 damage to target creature")
    void discardsAndDealsFiveDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new LightningAxe(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Pays {5} instead of discarding and deals 5 damage")
    void paysManaInsteadOfDiscarding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new LightningAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithDiscard(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deals exactly 5 damage to a creature that survives")
    void dealsExactlyFiveDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());

        harness.setHand(player1, List.of(new LightningAxe(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Havenwood Wurm");
    }

    @Test
    @DisplayName("Cannot cast without a discard or enough mana for the alternate cost")
    void cannotCastWithoutDiscardOrMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new LightningAxe()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card or pay {5}");
    }

    @Test
    @DisplayName("Rejects non-creature targets")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new LightningAxe(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, playerId, 1))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Discard is paid before resolution and remains paid if the target leaves")
    void discardRemainsPaidWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        harness.setHand(player1, List.of(new LightningAxe(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Axe");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Can target its controller's creature and discard a card before the spell in hand")
    void targetsOwnCreatureAndDiscardsEarlierCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HavenwoodWurm());
        harness.setHand(player1, List.of(new AshcoatBear(), new LightningAxe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithDiscard(player1, 1, target.getId(), 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player1, "Havenwood Wurm");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot discard Lightning Axe itself to pay its additional cost")
    void cannotDiscardTheSpellItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new LightningAxe()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
