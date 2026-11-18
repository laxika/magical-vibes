package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import java.util.Set;
import java.util.stream.Collectors;

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

        assertThat(PermanentGrants.protectionColorsOf(gd, target)).contains(CardColor.RED);
        assertThat(PermanentGrants.protectionColorsOf(gd, other)).doesNotContain(CardColor.RED);
        // The player does not gain protection in normal mode.
        assertThat(playerProtectionColors(player1.getId()))
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
        assertThat(playerProtectionColors(player1.getId())).contains(CardColor.WHITE);

        // Every permanent the controller controls gains protection from the chosen color.
        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(PermanentGrants.protectionColorsOf(gd, permanent)).contains(CardColor.WHITE);
        }
    }

    @Test
    @DisplayName("Fateful hour: protection from a color prevents combat damage to the protected player")
    void fatefulHourProtectionPreventsCombatDamageToPlayer() {
        // player2 has protection from red; player1 attacks with a red Russet Wolves.
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player2.getId(),
                new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.RED), player2.getId(),
                Duration.UNTIL_END_OF_TURN));

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

        assertThat(playerProtectionColors(player1.getId())).contains(CardColor.RED);

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
        assertThat(playerProtectionColors(player1.getId()))
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

        assertThat(playerProtectionColors(player1.getId())).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(playerProtectionColors(player1.getId()))
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

        assertThat(PermanentGrants.protectionColorsOf(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(PermanentGrants.protectionColorsOf(gd, other)).isEmpty();
        assertThat(playerProtectionColors(player1.getId())).isEmpty();
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

        assertThat(PermanentGrants.protectionColorsOf(gd, target)).containsExactly(CardColor.BLUE);
        assertThat(PermanentGrants.protectionColorsOf(gd, land)).containsExactly(CardColor.BLUE);
        assertThat(playerProtectionColors(player1.getId())).containsExactly(CardColor.BLUE);
        assertThat(PermanentGrants.protectionColorsOf(gd, opponent)).isEmpty();
        assertThat(playerProtectionColors(player2.getId())).isEmpty();
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

        assertThat(PermanentGrants.protectionColorsOf(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(PermanentGrants.protectionColorsOf(gd, other)).isEmpty();
        assertThat(playerProtectionColors(player1.getId())).isEmpty();
    }

    @Test
    void canTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.handleListChoice(player1, "WHITE");

        assertThat(PermanentGrants.protectionColorsOf(gd, land)).containsExactly(CardColor.WHITE);
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

        assertThat(PermanentGrants.protectionColorsOf(gd, target)).contains(CardColor.RED);
        assertThat(PermanentGrants.protectionColorsOf(gd, newcomer)).isEmpty();
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
        assertThat(PermanentGrants.protectionColorsOf(gd, remaining)).isEmpty();
        assertThat(playerProtectionColors(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Faith's Shield");
    }

    @Test
    void fatefulHourMakesAnExistingRedSpellTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new FiresOfUndeath(), new FaithsShield()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
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
        assertThat(PermanentGrants.protectionColorsOf(gd, target)).contains(CardColor.RED);
        assertThat(PermanentGrants.protectionColorsOf(gd, land)).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.protectionColorsOf(gd, target)).isEmpty();
        assertThat(PermanentGrants.protectionColorsOf(gd, land)).isEmpty();
    }

    private Set<CardColor> playerProtectionColors(UUID playerId) {
        return gd.floatingPlayerEffects(playerId, GrantProtectionFromColorUntilEndOfTurnEffect.class).stream()
                .map(effect -> ((GrantProtectionFromColorUntilEndOfTurnEffect) effect).color())
                .collect(Collectors.toSet());
    }
}
