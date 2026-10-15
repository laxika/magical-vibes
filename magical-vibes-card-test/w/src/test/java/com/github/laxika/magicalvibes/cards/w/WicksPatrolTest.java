package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WicksPatrol.class, AirElemental.class, ColossalDreadmaw.class, Forest.class,
        LeylineOfTheVoid.class, BruvacTheGrandiloquent.class, CarnageTyrant.class})
class WicksPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Milling three cards creates a reflexive targeted -X/-X ability")
    void millsThreeThenDebuffsAnOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(6);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(6);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A library with fewer than three cards does not create the reflexive ability")
    void doesNotDebuffWhenThreeCardsCannotBeMilled() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(6);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cards milled to exile still create the reflexive ability")
    void triggersWhenMilledCardsAreExiledInstead() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubling the mill does not suppress the reflexive ability")
    void triggersWhenBruvacDoublesTheMill() {
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("X uses newly milled cards and ignores the opponent's graveyard")
    void usesGreatestManaValueInControllersGraveyard() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new ColossalDreadmaw()));
        harness.setLibrary(player1, List.of(new Forest(), new AirElemental(), new Forest()));

        castAndResolveWicks();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("X is calculated at resolution and stays fixed afterward")
    void calculatesXOnlyWhenReflexiveAbilityResolves() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(6);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(6);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(6);
        harness.setGraveyard(player1, List.of(new AirElemental()));
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(6);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The debuff expires at the end of the turn")
    void debuffExpiresAtEndOfTurn() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(6);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The reflexive ability can target only an opponent's creature")
    void offersOnlyOpposingCreaturesAsTargets() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new ColossalDreadmaw()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Opposing creatures with hexproof are not offered as targets")
    void excludesHexproofFromTargetChoices() {
        harness.addToBattlefield(player2, new CarnageTyrant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("No target choice opens when every opposing creature has hexproof")
    void doesNotAskForAnIllegalHexproofTarget() {
        harness.addToBattlefield(player2, new CarnageTyrant());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Milling still happens when there are no legal opposing creatures")
    void millsWithoutLegalTargets() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new Forest());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private void castAndResolveWicks() {
        harness.castFromHand(player1, new WicksPatrol(), "{4}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
    }
}
