package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BloodlineKeeper;
import com.github.laxika.magicalvibes.cards.l.LordOfLineage;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.h.HeadwaterSentries;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousVoyage.class, RaptorCompanion.class, HeadwaterSentries.class, Island.class, JungleDelver.class,
        PryingBlade.class, BloodlineKeeper.class, LordOfLineage.class})
class PerilousVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("Bounces target and enters scry state when mana value is 2 or less")
    void bouncesAndScrysWhenManaValueAtMost2() {
        harness.addToBattlefield(player2, new RaptorCompanion()); // MV 2
        UUID targetId = harness.getPermanentId(player2, "Raptor Companion");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();

        // Creature bounced
        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInHand(player2, "Raptor Companion");

        // Scry triggered
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry triggers for MV 1 creature")
    void scryTriggersForManaValue1() {
        harness.addToBattlefield(player2, new JungleDelver()); // MV 1
        UUID targetId = harness.getPermanentId(player2, "Jungle Delver");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Jungle Delver");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("Scry completes and spell goes to graveyard")
    void scryCompletesAndSpellGoesToGraveyard() {
        harness.addToBattlefield(player2, new RaptorCompanion()); // MV 2
        UUID targetId = harness.getPermanentId(player2, "Raptor Companion");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Complete scry
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Perilous Voyage");
    }

    @Test
    @DisplayName("Bounces target but does NOT scry when mana value is greater than 2")
    void bouncesWithoutScryWhenManaValueAbove2() {
        harness.addToBattlefield(player2, new HeadwaterSentries()); // MV 4
        UUID targetId = harness.getPermanentId(player2, "Headwater Sentries");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();

        // Creature bounced
        harness.assertNotOnBattlefield(player2, "Headwater Sentries");
        harness.assertInHand(player2, "Headwater Sentries");

        // No scry — spell should fully resolve
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Perilous Voyage");
    }

    @Test
    @DisplayName("Cannot target own permanent")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player2, new RaptorCompanion()); // valid target so spell is playable
        harness.addToBattlefield(player1, new JungleDelver());
        UUID ownTargetId = harness.getPermanentId(player1, "Jungle Delver");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent you don't control");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new RaptorCompanion()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent you don't control");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution — no bounce, no scry")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        UUID targetId = harness.getPermanentId(player2, "Raptor Companion");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Perilous Voyage");
    }

    @Test
    void scriesForFaceDownCreatureRegardlessOfPrintedManaCost() {
        var target = harness.addToBattlefieldAndReturn(player2, new HeadwaterSentries());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Headwater Sentries");
        harness.assertInHand(player2, "Headwater Sentries");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void doesNotScryForTransformedCreatureWithFrontFaceManaValueAboveTwo() {
        var target = harness.addToBattlefieldAndReturn(player2, new BloodlineKeeper());
        target.setCard(target.getOriginalCard().getBackFaceCard());
        target.setTransformed(true);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Lord of Lineage");
        harness.assertInHand(player2, "Bloodline Keeper");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Perilous Voyage");
    }

    @Test
    void bouncesNoncreaturePermanentAndScries() {
        var target = harness.addToBattlefieldAndReturn(player2, new PryingBlade());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Prying Blade");
        harness.assertInHand(player2, "Prying Blade");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void returnsOpponentControlledPermanentToItsOwner() {
        var target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInHand(player1, "Raptor Companion");
        harness.assertNotInHand(player2, "Raptor Companion");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void doesNotResolveIfCasterGainsControlOfTarget() {
        var target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertNotInHand(player2, "Raptor Companion");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Perilous Voyage");
    }
}
