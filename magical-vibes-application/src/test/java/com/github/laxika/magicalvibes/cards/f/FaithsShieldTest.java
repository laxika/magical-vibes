package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.r.RussetWolves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaithsShield.class, DawntreaderElk.class, EvolvingWilds.class, RussetWolves.class, FiresOfUndeath.class})
class FaithsShieldTest extends BaseCardTest {

    @Test
    @DisplayName("With 6+ life, only the targeted permanent gains protection from the chosen color")
    void normalModeGrantsProtectionToTargetOnly() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent target = battlefield.stream().filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
        Permanent other = battlefield.stream().filter(p -> !p.getId().equals(targetId)).findFirst().orElseThrow();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(other.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
        // The player does not gain protection in normal mode.
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Fateful hour: you and each permanent you control gain protection from the chosen color")
    void fatefulHourGrantsProtectionToControllerAndAllPermanents() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 5);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "WHITE");

        // The controller gains protection from the chosen color.
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).contains(CardColor.WHITE);

        // Every permanent the controller controls gains protection from the chosen color.
        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(permanent.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        }
    }

    @Test
    @DisplayName("Fateful hour: protection from a color prevents combat damage to the protected player")
    void fatefulHourProtectionPreventsCombatDamageToPlayer() {
        // player2 has protection from red; player1 attacks with a red Russet Wolves.
        gd.playerProtectionFromColorsUntilEndOfTurn
                .computeIfAbsent(player2.getId(), k -> new HashSet<>()).add(CardColor.RED);

        Permanent giant = harness.addToBattlefieldAndReturn(player1, new RussetWolves());
        giant.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Russet Wolves's red combat damage is prevented; player2 stays at 20 life.
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Fateful hour: protection from a color prevents the protected player from being targeted by that color")
    void fatefulHourProtectionPreventsTargetingByColor() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FaithsShield(), new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 5);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).contains(CardColor.RED);

        // A red spell can no longer target the protected player (protection blocks all sources).
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Fateful hour: the spell still requires a target permanent you control (fizzles if removed)")
    void fatefulHourStillRequiresTarget() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 5);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution — the whole spell fizzles, even in fateful hour.
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.WHITE, CardColor.RED, CardColor.BLUE, CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Fateful hour: player protection is cleared at end of turn")
    void fatefulHourProtectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 5);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.RED);
    }

    @Test
    void sixLifeUsesOnlyTargetProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setLife(player1, 6);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
        assertThat(other.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>())).isEmpty();
    }

    @Test
    void lifeDroppingBeforeResolutionEnablesFatefulHour() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RussetWolves());
        harness.setLife(player1, 6);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.setLife(player1, 5);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.BLUE);
        assertThat(land.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.BLUE);
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).containsExactly(CardColor.BLUE);
        assertThat(opponent.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player2.getId(), new HashSet<>())).isEmpty();
    }

    @Test
    void lifeRisingBeforeResolutionDisablesFatefulHour() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.setLife(player1, 6);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.BLACK);
        assertThat(other.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>())).isEmpty();
    }

    @Test
    void canTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.handleListChoice(player1, "WHITE");

        assertThat(land.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.WHITE);
    }

    @Test
    void fatefulHourDoesNotProtectPermanentsEnteringLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new RussetWolves());

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(newcomer.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    void fatefulHourCannotTargetAnOpponentsPermanent() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fatefulHourDoesNotResolveWhenTargetChangesController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(remaining.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>())).isEmpty();
        harness.assertInGraveyard(player1, "Faith's Shield");
    }

    @Test
    void fatefulHourMakesAnExistingRedSpellTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FiresOfUndeath(), new FaithsShield()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        harness.assertInGraveyard(player1, "Fires of Undeath");
    }

    @Test
    void resolvedFatefulHourPreventsCombatDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RussetWolves());
        attacker.setSummoningSick(false);
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
    }

    @Test
    void permanentProtectionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(land.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(land.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }
}
