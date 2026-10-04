package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.k.Kindle;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.m.Manakin;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.r.RootwaterShaman;
import com.github.laxika.magicalvibes.cards.s.ShimmeringWings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandToHand.class, Kindle.class, Manakin.class, MoggFanatic.class,
        RootwaterShaman.class, ShimmeringWings.class, Humility.class, Opalescence.class})
class HandToHandTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can't cast an instant during combat")
    void controllerCantCastInstantDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Kindle");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent can't cast an instant during combat either")
    void opponentCantCastInstantDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.setHand(player2, List.of(new Kindle()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Kindle");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Instants can still be cast outside combat")
    void instantCastableOutsideCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Non-mana activated abilities can't be activated during combat")
    void nonManaAbilityBlockedDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.addToBattlefield(player2, new MoggFanatic());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during combat");
        harness.assertOnBattlefield(player2, "Mogg Fanatic");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Mana abilities still work during combat")
    void manaAbilityAllowedDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        Permanent manakin = addCreatureReady(player2, new Manakin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player2, 0, null, null);

        assertThat(manakin.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-mana activated abilities work outside combat")
    void nonManaAbilityAllowedOutsideCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.addToBattlefield(player2, new MoggFanatic());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {
            "BEGINNING_OF_COMBAT", "DECLARE_ATTACKERS", "DECLARE_BLOCKERS", "COMBAT_DAMAGE", "END_OF_COMBAT"
    })
    @DisplayName("The restriction applies throughout the combat phase")
    void actionsBlockedInEveryCombatStep(TurnStep step) {
        harness.addToBattlefield(player2, new HandToHand());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during combat");

        harness.assertInHand(player1, "Kindle");
        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The restriction ends when combat ends")
    void actionsAllowedAfterCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.setHand(player2, List.of(new Kindle()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player2, "Kindle");
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @Test
    @DisplayName("A spell already on the stack still resolves when Hand to Hand enters")
    void existingInstantStillResolves() {
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player2, new HandToHand());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Kindle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-instant spells with flash can still be cast during combat")
    void auraWithFlashCanBeCastDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new RootwaterShaman());
        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castEnchantment(player1, 0, shaman.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shimmering Wings");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hand to Hand does not restrict combat after continuously losing its abilities")
    void continuousAbilityRemovalDisablesCombatRestriction() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new HandToHand());
        harness.addToBattlefield(player1, new Humility());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Kindle");
        assertThat(gd.stack).isEmpty();
    }
}
