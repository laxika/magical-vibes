package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmamentDragon;
import com.github.laxika.magicalvibes.cards.b.BoulderbornDragon;
import com.github.laxika.magicalvibes.cards.f.FortressKinGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CausticExhale.class, BoulderbornDragon.class, FortressKinGuard.class, ArmamentDragon.class})
class CausticExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature -3/-3 when a Dragon is beheld from the battlefield")
    void beheldDragonPermanentGivesMinusThreeMinusThree() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BoulderbornDragon());
        harness.addToBattlefield(player2, new FortressKinGuard());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Fortress Kin-Guard"),
                List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fortress Kin-Guard");
    }

    @Test
    @DisplayName("Gives a target creature -3/-3 when a Dragon is beheld from hand")
    void beheldDragonCardGivesMinusThreeMinusThree() {
        harness.addToBattlefield(player2, new FortressKinGuard());
        harness.setHand(player1, List.of(new CausticExhale(), new BoulderbornDragon()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Fortress Kin-Guard"),
                List.of(), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fortress Kin-Guard");
    }

    @Test
    @DisplayName("Can pay {1} instead of beholding a Dragon")
    void paysAdditionalManaWithoutDragon() {
        harness.addToBattlefield(player2, new FortressKinGuard());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fortress Kin-Guard"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fortress Kin-Guard");
    }

    @Test
    @DisplayName("Cannot cast for only {B} without a Dragon")
    void requiresDragonOrAdditionalMana() {
        harness.addToBattlefield(player2, new FortressKinGuard());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Fortress Kin-Guard")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining to behold a controlled Dragon still costs an additional mana")
    void paysAdditionalManaWhenDecliningControlledDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ArmamentDragon());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, dragon.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, dragon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to reveal a Dragon from hand still costs an additional mana")
    void paysAdditionalManaWhenDecliningDragonInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmamentDragon());
        harness.setHand(player1, List.of(new CausticExhale(), new ArmamentDragon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Armament Dragon");
    }

    @Test
    @DisplayName("Cannot behold an opponent's Dragon")
    void cannotBeholdOpponentsDragon() {
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new ArmamentDragon());
        Permanent opposingDragon = harness.addToBattlefieldAndReturn(player2, new ArmamentDragon());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, ownDragon.getId(),
                List.of(opposingDragon.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The -3/-3 expires at end of turn and beholding does not tap or sacrifice the Dragon")
    void modifierExpiresAndBeheldDragonRemains() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ArmamentDragon());
        harness.setHand(player1, List.of(new CausticExhale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithBehold(player1, 0, dragon.getId(), List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Armament Dragon");
        assertThat(dragon.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, dragon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Beholding a Dragon from hand publicly reveals it without discarding it")
    void dragonFromHandIsRevealed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmamentDragon());
        harness.setHand(player1, List.of(new CausticExhale(), new BoulderbornDragon()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));

        assertThat(gameLogContains("reveals")).isTrue();
        harness.assertInHand(player1, "Boulderborn Dragon");
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }
}
