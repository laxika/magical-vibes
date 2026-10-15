package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PitilessFists.class, HillGiant.class, GrizzlyBears.class, Naturalize.class})
class PitilessFistsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoosted() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PitilessFists());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(5);
    }

    @Test
    @DisplayName("The ETB ability makes the enchanted creature fight an opposing creature")
    void etbAbilityFights() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB ability resolves without an opposing creature")
    void etbAbilityResolvesWithoutOpposingCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The fight still happens if the Aura is destroyed in response")
    void fightUsesLastKnownAttachmentAfterAuraIsDestroyed() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PitilessFists(), new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof PitilessFists).findFirst().orElseThrow();
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pitiless Fists");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both creatures count as having fought this turn")
    void fightIsRecordedForBothCreatures() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.permanentsThatFoughtThisTurn).contains(giant.getId(), opponentGiant.getId());
    }

    @Test
    @DisplayName("The Aura can enchant only a creature its controller controls")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
