package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hurloon Shaman")
@CardUsed({HurloonShaman.class, RedwoodTreefolk.class, WindingCanyons.class})
class HurloonShamanTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, each player with a single land sacrifices it automatically")
    void deathTriggerMakesEachPlayerSacrificeTheirOnlyLand() {
        addCreatureReady(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player2, new WindingCanyons());
        setupCombatWhereShamanDies();

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hurloon Shaman");
        harness.assertNotOnBattlefield(player1, "Winding Canyons");
        harness.assertNotOnBattlefield(player2, "Winding Canyons");
    }

    @Test
    @DisplayName("A player with multiple lands chooses which one to sacrifice")
    void playerWithMultipleLandsChooses() {
        addCreatureReady(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player2, new WindingCanyons());
        setupCombatWhereShamanDies();

        resolveCombat();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        List<UUID> chosen = findPermanents(player1, "Winding Canyons").stream()
                .limit(1)
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, chosen);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countLands(player1)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Winding Canyons");
    }

    @Test
    @DisplayName("Only lands are sacrificed — other permanents are untouched")
    void onlyLandsAreSacrificed() {
        addCreatureReady(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        // Player2 has no lands at all — unaffected.
        setupCombatWhereShamanDies();

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hurloon Shaman");
        harness.assertOnBattlefield(player1, "Redwood Treefolk");
        harness.assertOnBattlefield(player2, "Redwood Treefolk");
    }

    @Test
    @DisplayName("Each player chooses independently when each controls multiple lands")
    void eachPlayerChoosesTheirOwnLand() {
        addCreatureReady(player1, new HurloonShaman());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player2, new WindingCanyons());
        harness.addToBattlefield(player2, new WindingCanyons());
        setupCombatWhereShamanDies();

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());

        List<UUID> player1Chosen = findPermanents(player1, "Winding Canyons").stream()
                .limit(1)
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, player1Chosen);

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());

        List<UUID> player2Chosen = findPermanents(player2, "Winding Canyons").stream()
                .limit(1)
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player2, player2Chosen);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countLands(player1)).isEqualTo(1);
        assertThat(countLands(player2)).isEqualTo(1);
    }

    private long countLands(com.github.laxika.magicalvibes.model.Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    /** Attacks with the Shaman into a blocker that kills it in combat damage. */
    private void setupCombatWhereShamanDies() {
        GameData gd = harness.getGameData();
        Permanent shaman = findPermanent(player1, "Hurloon Shaman");
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());
        int shamanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackers(player1, List.of(shamanIndex));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, shamanIndex)));
    }
}
