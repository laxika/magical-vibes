package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetrayersBargain.class, GloriousAnthem.class, GrizzlyBears.class, Plains.class})
class BetrayersBargainTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndExilesCreatureKilledByDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void sacrificesEnchantmentInsteadOfCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void paysManaInsteadOfSacrificing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void rejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonCreatureNonEnchantmentSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    @Test
    void exilesTargetSacrificedLaterEvenWhenAllDamageWasPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(5);
        harness.setHand(player1, List.of(new BetrayersBargain(), new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();

        harness.castInstantWithSacrifice(player1, 0, otherTarget.getId(), target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.passBothPriorities();
    }

    @Test
    void exilesTargetSacrificedLaterAfterSurvivingDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(4);
        harness.setHand(player1, List.of(new BetrayersBargain(), new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(1);

        harness.castInstantWithSacrifice(player1, 0, otherTarget.getId(), target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.passBothPriorities();
    }

    @Test
    void rejectsCastingWithoutSacrificeOrEnoughAdditionalMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void rejectsSacrificingOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayersBargain()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
