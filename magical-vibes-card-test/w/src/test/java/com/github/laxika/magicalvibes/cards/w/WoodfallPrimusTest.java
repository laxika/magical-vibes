package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodfallPrimus.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, LeoninScimitar.class})
class WoodfallPrimusTest extends BaseCardTest {

    private void castWoodfallPrimus(UUID targetId) {
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castCreature(player1, 0, targetId);
    }

    @Test
    @DisplayName("ETB destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castWoodfallPrimus(harness.getPermanentId(player2, "Leonin Scimitar"));

        // Resolve creature spell, then ETB triggered ability
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Woodfall Primus");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        castWoodfallPrimus(harness.getPermanentId(player2, "Glorious Anthem"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB destroys target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        castWoodfallPrimus(harness.getPermanentId(player2, "Forest"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature permanent");
    }

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castWoodfallPrimus(harness.getPermanentId(player2, "Leonin Scimitar"));

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can cast without a target when only creatures exist")
    void canCastWithoutTargetWhenOnlyCreaturesExist() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Woodfall Primus");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Woodfall Primus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void persistReturnsWithCounterAndDestroysAnotherPermanent() {
        Permanent primus = harness.addToBattlefieldAndReturn(player1, new WoodfallPrimus());
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, primus));
        harness.assertInGraveyard(player1, "Woodfall Primus");
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Woodfall Primus");
        assertThat(returned.getId()).isNotEqualTo(primus.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Woodfall Primus");
        harness.handlePermanentChosen(player1, landId);
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Forest");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, returned));
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Woodfall Primus");
        harness.assertInGraveyard(player1, "Woodfall Primus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void persistDoesNotTriggerWithExistingMinusCounter() {
        Permanent primus = harness.addToBattlefieldAndReturn(player1, new WoodfallPrimus());
        primus.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, primus));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Woodfall Primus");
        harness.assertInGraveyard(player1, "Woodfall Primus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void persistDoesNothingIfCardLeavesGraveyardBeforeResolution() {
        WoodfallPrimus card = new WoodfallPrimus();
        Permanent primus = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, primus));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(card));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Woodfall Primus");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.findExiledCard(card.getId()).card()).isEqualTo(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        addCreatureReady(player1, new WoodfallPrimus());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Woodfall Primus");
    }

    @Test
    void persistReturnsToOwnerRatherThanPreviousController() {
        WoodfallPrimus card = new WoodfallPrimus();
        card.setOwnerId(player1.getId());
        Permanent primus = harness.addToBattlefieldAndReturn(player2, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, primus));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Woodfall Primus");
        harness.assertNotOnBattlefield(player2, "Woodfall Primus");
        assertThat(findPermanent(player1, "Woodfall Primus")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbCanDestroyControllersOwnNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        castWoodfallPrimus(harness.getPermanentId(player1, "Forest"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Woodfall Primus");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }
}
