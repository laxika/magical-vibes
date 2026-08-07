package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hurloon Shaman")
class HurloonShamanTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, each player with a single land sacrifices it automatically")
    void deathTriggerMakesEachPlayerSacrificeTheirOnlyLand() {
        harness.addToBattlefield(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        setupCombatWhereShamanDies();

        harness.passBothPriorities(); // combat damage — Shaman dies, trigger on stack
        harness.passBothPriorities(); // trigger resolves

        harness.assertInGraveyard(player1, "Hurloon Shaman");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A player with multiple lands chooses which one to sacrifice")
    void playerWithMultipleLandsChooses() {
        harness.addToBattlefield(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        setupCombatWhereShamanDies();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        List<UUID> chosen = findPermanents(player1, "Mountain").stream().limit(1).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, chosen);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countLands(player1)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Only lands are sacrificed — other permanents are untouched")
    void onlyLandsAreSacrificed() {
        harness.addToBattlefield(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Player2 has no lands at all — unaffected.
        setupCombatWhereShamanDies();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hurloon Shaman");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    private long countLands(com.github.laxika.magicalvibes.model.Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    /**
     * Attacks with the Shaman into a 2/3 body's worth of blocker so it dies in combat damage.
     */
    private void setupCombatWhereShamanDies() {
        GameData gd = harness.getGameData();
        Permanent shaman = findPermanent(player1, "Hurloon Shaman");
        shaman.setSummoningSick(false);
        shaman.setAttacking(true);

        Permanent blocker = new Permanent(new HillGiant()); // 3/3 — kills the 2/3 Shaman and survives
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
