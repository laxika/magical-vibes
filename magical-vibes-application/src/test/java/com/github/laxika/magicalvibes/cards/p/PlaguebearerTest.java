package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DauthiJackal;
import com.github.laxika.magicalvibes.cards.m.MemoryCrystal;
import com.github.laxika.magicalvibes.cards.m.MoggAssassin;
import com.github.laxika.magicalvibes.cards.t.ThopterSquadron;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Plaguebearer.class, MoggAssassin.class, DauthiJackal.class, MemoryCrystal.class,
        ThopterSquadron.class})
class PlaguebearerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature with mana value X")
    void destroysNonblackCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoggAssassin()).getId();
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.activateAbility(player1, 0, 3, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Assassin");
        harness.assertInGraveyard(player2, "Mogg Assassin");
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value is not X")
    void cannotTargetCreatureWithDifferentManaValue() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoggAssassin()).getId();
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new DauthiJackal()).getId();
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MemoryCrystal()).getId();
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays for both X symbols and the black mana")
    void paysDoubleXAndBlack() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoggAssassin()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 3, target);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mogg Assassin");
    }

    @Test
    @DisplayName("Cannot activate with enough mana for only one X symbol")
    void cannotPayForOnlyOneX() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoggAssassin()).getId();
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Mogg Assassin");
    }

    @Test
    @DisplayName("Cannot pay the black symbol with colorless mana")
    void requiresBlackMana() {
        harness.addToBattlefield(player1, new Plaguebearer());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoggAssassin()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and target its controller's creature")
    void canActivateRepeatedlyWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new Plaguebearer()).tap();
        UUID ownTarget = harness.addToBattlefieldAndReturn(player1, new MoggAssassin()).getId();
        UUID opposingTarget = harness.addToBattlefieldAndReturn(player2, new MoggAssassin()).getId();
        harness.addMana(player1, ManaColor.BLACK, 14);

        harness.activateAbility(player1, 0, 3, ownTarget);
        harness.activateAbility(player1, 0, 3, opposingTarget);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mogg Assassin");
        harness.assertInGraveyard(player2, "Mogg Assassin");
        harness.assertOnBattlefield(player1, "Plaguebearer");
    }

    @Test
    @DisplayName("X can be zero to destroy a colorless creature token for one black mana")
    void destroysColorlessTokenWithXZero() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.setHand(player1, List.of(new ThopterSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        UUID token = harness.getPermanentId(player1, "Thopter");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, token);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thopter");
        harness.assertOnBattlefield(player1, "Thopter Squadron");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
