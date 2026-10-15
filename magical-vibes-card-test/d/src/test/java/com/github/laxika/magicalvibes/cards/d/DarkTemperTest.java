package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.m.Megrim;
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

@CardUsed({DarkTemper.class, ChoMannoRevolutionary.class, GloriousAnthem.class, HillGiant.class,
        MassOfGhouls.class, Megrim.class})
class DarkTemperTest extends BaseCardTest {

    @Test
    @DisplayName("Without a black permanent — deals 2 damage to target creature")
    void withoutBlackPermanentDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        // 3/3 survives 2 damage, which is marked (not destroyed).
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("With a black permanent — destroys target creature instead of dealing damage")
    void withBlackPermanentDestroysInstead() {
        harness.addToBattlefield(player1, new MassOfGhouls()); // black permanent
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's black permanent does not enable destruction")
    void opponentsBlackPermanentDoesNotEnableDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Mass of Ghouls");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A black card in hand does not enable destruction")
    void blackCardInHandDoesNotEnableDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DarkTemper(), new MassOfGhouls()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A black permanent entering before resolution enables destruction")
    void blackPermanentEnteringBeforeResolutionEnablesDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new MassOfGhouls());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Losing the only black permanent before resolution restores the damage effect")
    void losingBlackPermanentBeforeResolutionRestoresDamage() {
        Permanent blackPermanent = harness.addToBattlefieldAndReturn(player1, new MassOfGhouls());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DarkTemper(), new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, blackPermanent.getId());

        harness.assertInGraveyard(player1, "Mass of Ghouls");
        assertThat(blackPermanent.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A black enchantment enables destruction despite damage prevention")
    void blackEnchantmentEnablesDestructionDespiteDamagePrevention() {
        harness.addToBattlefield(player1, new Megrim());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Cho-Manno, Revolutionary");
        harness.assertInGraveyard(player2, "Cho-Manno, Revolutionary");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without a black permanent, damage prevention prevents the damage")
    void damagePreventionAppliesWithoutBlackPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        harness.setHand(player1, List.of(new DarkTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
