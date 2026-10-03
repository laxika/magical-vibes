package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.n.NowhereToRun;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharmedClothier.class, GrizzlyBears.class, Shock.class, NowhereToRun.class})
class CharmedClothierTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Royal Role attached to another creature you control")
    void createsRoyalRoleAttachedToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCharmedClothier(target.getId());

        Permanent role = roleAttachedTo(target.getId());
        assertThat(target.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CharmedClothier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("The Royal Role grants ward {1} to the enchanted creature")
    void royalRoleGrantsWard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCharmedClothier(target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A new Royal Role replaces the older Role controlled by the same player")
    void newRoyalRoleReplacesOlderRole() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCharmedClothier(target.getId());
        UUID oldRoleId = roleAttachedTo(target.getId()).getId();

        castCharmedClothier(target.getId());

        assertThat(roleAttachedTo(target.getId()).getId()).isNotEqualTo(oldRoleId);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(oldRoleId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> target.getId().equals(permanent.getAttachedTo()))
                .hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying ward allows the opponent's spell to resolve")
    void payingWardAllowsSpellToResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCharmedClothier(target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Can enter without another creature and does not create a Role on itself")
    void entersWithoutAnotherCreature() {
        harness.setHand(player1, List.of(new CharmedClothier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Charmed Clothier");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROLE));
    }
    @Test
    @DisplayName("Nowhere to Run suppresses the ward ability granted by the Royal Role")
    void nowhereToRunSuppressesRoyalWard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCharmedClothier(target.getId());
        harness.addToBattlefield(player2, new NowhereToRun());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
    private void castCharmedClothier(UUID targetId) {
        harness.setHand(player1, List.of(new CharmedClothier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent roleAttachedTo(UUID targetId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROLE))
                .filter(permanent -> targetId.equals(permanent.getAttachedTo()))
                .findFirst()
                .orElseThrow();
    }
}
