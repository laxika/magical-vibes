package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RalIzzetViceroy;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Plaguecrafter")
@CardUsed({Plaguecrafter.class, GrizzlyBears.class, Shock.class, ZuranOrb.class,
        RalIzzetViceroy.class, AssaultSuit.class})
class PlaguecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices a creature, with all choices resolving together")
    void eachPlayerSacrificesCreature() {
        GameData gd = harness.getGameData();
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentBears.getId()));

        harness.assertInGraveyard(player1, "Plaguecrafter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player without a creature or planeswalker discards a card")
    void playerWithoutSacrificeTargetDiscards() {
        Shock discard = new Shock();
        harness.setHand(player2, List.of(discard));
        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Plaguecrafter");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A noncreature permanent does not satisfy the sacrifice requirement")
    void noncreaturePermanentDoesNotSatisfyRequirement() {
        Shock discard = new Shock();
        harness.setHand(player2, List.of(discard));
        harness.addToBattlefield(player2, new ZuranOrb());
        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Zuran Orb");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("A player may sacrifice a planeswalker instead of a creature")
    void mayChoosePlaneswalkerWhenCreatureIsAlsoAvailable() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        harness.addToBattlefield(player2, new Plaguecrafter());
        harness.setHand(player2, List.of(new Plaguecrafter()));

        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.assertOnBattlefield(player1, "Plaguecrafter");
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        harness.assertInGraveyard(player1, "Plaguecrafter");
        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        harness.assertOnBattlefield(player2, "Plaguecrafter");
        harness.assertInHand(player2, "Plaguecrafter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A lone planeswalker satisfies the sacrifice requirement without discarding")
    void lonePlaneswalkerIsSacrificed() {
        harness.addToBattlefield(player2, new RalIzzetViceroy());
        harness.setHand(player2, List.of(new Plaguecrafter()));

        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plaguecrafter");
        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        harness.assertInHand(player2, "Plaguecrafter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A player with no eligible permanent and no cards does nothing")
    void emptyHandNeedsNoDiscardChoice() {
        harness.setHand(player2, List.of());

        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plaguecrafter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Both players choose their discards before either card is discarded")
    void bothPlayersDiscardSimultaneouslyAfterSourceDies() {
        removeSourceBeforeTriggerResolves();
        harness.setHand(player1, List.of(new RalIzzetViceroy()));
        harness.setHand(player2, List.of(new RalIzzetViceroy()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Ral, Izzet Viceroy");
        harness.assertInHand(player2, "Ral, Izzet Viceroy");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Ral, Izzet Viceroy");
        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("An empty-handed later player is skipped after the first player's discard choice")
    void skipsEmptyHandAfterEarlierPlayerChoosesDiscard() {
        removeSourceBeforeTriggerResolves();
        harness.setHand(player1, List.of(new RalIzzetViceroy()));
        harness.setHand(player2, List.of());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ral, Izzet Viceroy");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A creature that cannot be sacrificed survives and its controller discards")
    void sacrificeProhibitionRequiresDiscard() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new Plaguecrafter());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new AssaultSuit());
        suit.setAttachedTo(protectedCreature.getId());
        harness.setHand(player2, List.of(new RalIzzetViceroy()));

        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plaguecrafter");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        harness.assertOnBattlefield(player2, "Plaguecrafter");
        harness.assertOnBattlefield(player2, "Assault Suit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void removeSourceBeforeTriggerResolves() {
        castPlaguecrafter();
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInGraveyard(player1, "Plaguecrafter");
        assertThat(gd.stack).isNotEmpty();
    }

    private void castPlaguecrafter() {
        harness.castFromHand(player1, new Plaguecrafter(), "{2}{B}");
    }
}
