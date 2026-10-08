package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PossibilityStorm;
import com.github.laxika.magicalvibes.cards.r.RalZarek;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatchRelease.class, FountainOfYouth.class, GrizzlyBears.class, Juggernaut.class,
        GloriousAnthem.class, Plains.class, PossibilityStorm.class, RalZarek.class})
class CatchReleaseTest extends BaseCardTest {

    private static final int CATCH = 0;
    private static final int RELEASE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Catch gains control of, untaps, and gives haste to the target permanent")
    void catchGainsControlUntapsAndGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, CATCH, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Release requires distinct artifact and creature sacrifices when possible")
    void releaseRequiresDistinctSacrificesWhenPossible() {
        harness.addToBattlefield(player2, new Juggernaut());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, RELEASE, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(firstCreature.getId()));

        harness.assertInGraveyard(player2, "Juggernaut");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(secondCreature.getId());
    }

    @Test
    @DisplayName("Fuse resolves Catch before Release")
    void fuseResolvesCatchThenRelease() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, FUSE, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void catchControlAndHasteExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, CATCH, List.of(target.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void catchCanUntapOwnNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        target.tap();
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, CATCH, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void releaseSacrificesEveryListedTypeForBothPlayers() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new Plains());
        harness.enterBattlefieldAndReturn(player1, new RalZarek());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Plains());
        harness.enterBattlefieldAndReturn(player2, new RalZarek());
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, RELEASE, List.of());
        harness.passBothPriorities();

        for (var player : List.of(player1, player2)) {
            assertThat(gd.playerBattlefields.get(player.getId())).isEmpty();
            harness.assertInGraveyard(player, "Fountain of Youth");
            harness.assertInGraveyard(player, "Grizzly Bears");
            harness.assertInGraveyard(player, "Glorious Anthem");
            harness.assertInGraveyard(player, "Plains");
            harness.assertInGraveyard(player, "Ral Zarek");
        }
    }

    @Test
    void releaseSacrificesLoneArtifactCreatureOnlyOnce() {
        harness.addToBattlefield(player2, new Juggernaut());
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, RELEASE, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Juggernaut");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Juggernaut");
    }

    @Test
    void releaseCollectsChoicesInTurnOrderBeforeSacrificing() {
        Permanent firstChoice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstSurvivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondChoice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondSurvivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, RELEASE, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(firstChoice.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstChoice, firstSurvivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondChoice, secondSurvivor);

        harness.handleMultiplePermanentsChosen(player2, List.of(secondChoice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(firstSurvivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(secondSurvivor);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void fusedSpellDoesNotResolveReleaseWhenItsOnlyTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, FUSE, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Catch // Release");
    }

    @Test
    void castingFromExileDoesNotOfferFuse() {
        harness.addToBattlefield(player1, new PossibilityStorm());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new CatchRelease()));
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, RELEASE, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).hasSize(2);
        assertThat(choice.options()).noneMatch(option -> option.startsWith("Fuse"));
    }
}
