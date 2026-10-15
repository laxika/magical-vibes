package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({AnnexSentry.class, GrizzlyBears.class, HillGiant.class, LeoninScimitar.class, Unsummon.class})
class AnnexSentryTest extends BaseCardTest {

    private void castAndResolve(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AnnexSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles an opponent's creature with mana value 3 or less")
    void etbExilesOpponentCreatureWithManaValueAtMostThree() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB exiles an opponent's artifact and returns it when Annex Sentry leaves")
    void etbExilesArtifactAndReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID sentryId = harness.getPermanentId(player1, "Annex Sentry");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sentryId);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("ETB does not trigger when only illegal permanents are available")
    void etbSkipsIllegalTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AnnexSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Annex Sentry");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Combat damage gives a poison counter in addition to life loss")
    void combatDamageAppliesToxic() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new AnnexSentry());
        sentry.setSummoningSick(false);
        sentry.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The target is not exiled if Annex Sentry leaves before its ability resolves")
    void sourceLeavesBeforeAbilityResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new AnnexSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Annex Sentry"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Annex Sentry");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature with mana value exactly three is a legal target")
    void exilesArtifactCreatureAtManaValueBoundary() {
        harness.addToBattlefield(player2, new AnnexSentry());
        UUID targetId = harness.getPermanentId(player2, "Annex Sentry");

        castAndResolve(targetId);

        harness.assertOnBattlefield(player1, "Annex Sentry");
        harness.assertNotOnBattlefield(player2, "Annex Sentry");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Annex Sentry"));
    }
}
