package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DualSunAdepts;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.OchranAssassin;
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

@CardUsed({AllFatesStalker.class, DualSunAdepts.class, Forest.class, GrizzlyBears.class, Murder.class, OchranAssassin.class})
class AllFatesStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one target non-Assassin creature")
    void etbExilesNonAssassinCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB can resolve with no target")
    void etbCanResolveWithNoTarget() {
        prepareToCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AllFatesStalker);
    }

    @Test
    @DisplayName("An exiled creature returns when All-Fates Stalker leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(targetId);

        Permanent source = findPermanent(player1, "All-Fates Stalker");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An Assassin creature cannot be targeted")
    void cannotTargetAssassinCreature() {
        harness.addToBattlefield(player2, new OchranAssassin());
        UUID assassinId = harness.getPermanentId(player2, "Ochran Assassin");
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, assassinId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature permanent cannot be targeted")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Warp casts All-Fates Stalker for its alternate cost")
    void warpCastsForAlternateCost() {
        harness.setHand(player1, List.of(new AllFatesStalker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AllFatesStalker);
    }

    @Test
    @DisplayName("The optional exile may be declined even when a legal target exists")
    void canDeclineExileWithLegalTarget() {
        harness.addToBattlefield(player2, new DualSunAdepts());
        prepareToCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "All-Fates Stalker");
        harness.assertOnBattlefield(player2, "Dual-Sun Adepts");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exile does nothing if All-Fates Stalker leaves before its trigger resolves")
    void sourceLeavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new DualSunAdepts());
        UUID targetId = harness.getPermanentId(player2, "Dual-Sun Adepts");
        prepareToCast();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "All-Fates Stalker");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sourceId);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "All-Fates Stalker");
        harness.assertOnBattlefield(player2, "Dual-Sun Adepts");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exile trigger does nothing when its target leaves before resolution")
    void targetLeavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new DualSunAdepts());
        UUID targetId = harness.getPermanentId(player2, "Dual-Sun Adepts");
        prepareToCast();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "All-Fates Stalker");
        harness.assertInGraveyard(player2, "Dual-Sun Adepts");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Warp returns a friendly exiled creature and permits a later full-cost cast")
    void warpReturnsFriendlyCreatureAndCanBeRecastOnLaterTurn() {
        AllFatesStalker stalker = new AllFatesStalker();
        harness.addToBattlefield(player1, new DualSunAdepts());
        UUID targetId = harness.getPermanentId(player1, "Dual-Sun Adepts");
        harness.setHand(player1, List.of(stalker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, targetId);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Dual-Sun Adepts");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dual-Sun Adepts"));

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "All-Fates Stalker");
        harness.assertOnBattlefield(player1, "Dual-Sun Adepts");
        assertThat(gd.findExiledCard(stalker.getId())).isNotNull();

        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, stalker.getId(),
                harness.getPermanentId(player1, "Dual-Sun Adepts")))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, stalker.getId(),
                harness.getPermanentId(player1, "Dual-Sun Adepts"));
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "All-Fates Stalker");
        harness.assertNotOnBattlefield(player1, "Dual-Sun Adepts");

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "All-Fates Stalker");
        harness.assertNotOnBattlefield(player1, "Dual-Sun Adepts");
        assertThat(gd.findExiledCard(stalker.getId())).isNull();
    }

    @Test
    @DisplayName("Warp's delayed exile waits for players to pass priority at the end step")
    void warpExileUsesTheStack() {
        AllFatesStalker stalker = new AllFatesStalker();
        harness.setHand(player1, List.of(stalker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "All-Fates Stalker");
        assertThat(gd.findExiledCard(stalker.getId())).isNull();
        assertThat(gd.stack).isNotEmpty();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "All-Fates Stalker");
        assertThat(gd.findExiledCard(stalker.getId())).isNotNull();
    }

    @Test
    @DisplayName("A normal hand cast does not exile All-Fates Stalker at the end step")
    void normalCastStaysOnBattlefieldAtEndStep() {
        prepareToCast();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "All-Fates Stalker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AllFatesStalker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();
    }
}
