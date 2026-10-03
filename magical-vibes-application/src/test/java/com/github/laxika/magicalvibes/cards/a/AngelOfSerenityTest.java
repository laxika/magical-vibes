package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfSerenity.class, GrizzlyBears.class, HillGiant.class, Mountain.class, Unsummon.class,
        ArmoryGuard.class, RubblebackRhino.class, UltimatePrice.class, Humble.class})
class AngelOfSerenityTest extends BaseCardTest {

    private AngelOfSerenity castAngel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        AngelOfSerenity angel = new AngelOfSerenity();
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell; the ETB choice opens
        return angel;
    }

    private UUID graveyardCardId(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerGraveyards.get(player.getId()).stream()
                .filter(c -> c.getName().equals(cardName))
                .findFirst().orElseThrow().getId();
    }

    @Test
    @DisplayName("ETB exiles chosen creatures from the battlefield and creature cards from graveyards")
    void etbExilesAcrossBothZones() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getCard().getId();
        harness.setGraveyard(player2, List.of(new HillGiant()));

        castAngel();
        UUID giantId = graveyardCardId(player2, "Hill Giant");

        harness.handleMultipleCardsChosen(player1, List.of(bearsId, giantId));
        resolveExileTrigger();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("Choosing no targets exiles nothing")
    void choosingNoTargetsExilesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAngel();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled cards return to their owners' hands when the Angel leaves the battlefield")
    void exiledCardsReturnToOwnersHandsWhenAngelLeaves() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getCard().getId();
        harness.setGraveyard(player2, List.of(new HillGiant()));

        castAngel();
        UUID giantId = graveyardCardId(player2, "Hill Giant");
        harness.handleMultipleCardsChosen(player1, List.of(bearsId, giantId));
        resolveExileTrigger();

        // Bounce the Angel — the exiled cards go to their owner's hand, not back where they came from.
        UUID angelPermanentId = harness.getPermanentId(player1, "Angel of Serenity");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, angelPermanentId);

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Hill Giant");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void losingAbilitiesBeforeLeavingKeepsLinkedCardsExiled() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getCard().getId();
        castAngel();
        harness.handleMultipleCardsChosen(player1, List.of(bearsId));
        resolveExileTrigger();
        UUID angelPermanentId = harness.getPermanentId(player1, "Angel of Serenity");
        harness.setHand(player2, List.of(new Humble(), new Unsummon()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveInstant(player2, 0, angelPermanentId));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveInstant(player2, 0, angelPermanentId));

        harness.assertNotOnBattlefield(player1, "Angel of Serenity");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bearsId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Angel itself is not a legal choice — it exiles only other creatures")
    void angelIsNotALegalChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        AngelOfSerenity angel = castAngel();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(angel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature card in a graveyard is not a legal choice")
    void noncreatureGraveyardCardIsNotALegalChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new Mountain()));

        castAngel();
        UUID mountainId = graveyardCardId(player2, "Mountain");

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At most three cards may be chosen")
    void atMostThreeCardsMayBeChosen() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears(),
                new HillGiant()));

        castAngel();
        List<UUID> allFour = gd.playerGraveyards.get(player2.getId()).stream().map(Card::getId).toList();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, allFour))
                .isInstanceOf(IllegalStateException.class);
    }

    private void resolveExileTrigger() {
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

    @Test
    @DisplayName("The controller may decline exile after choosing targets")
    void mayDeclineExileOnResolution() {
        ArmoryGuard guard = new ArmoryGuard();
        harness.addToBattlefield(player2, guard);
        castAngel();
        harness.handleMultipleCardsChosen(player1, List.of(guard.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Armory Guard");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A battlefield target that dies before resolution is not exiled from its graveyard")
    void dyingBattlefieldTargetBecomesIllegal() {
        ArmoryGuard dyingGuard = new ArmoryGuard();
        ArmoryGuard survivingGuard = new ArmoryGuard();
        UUID dyingPermanentId = harness.addToBattlefieldAndReturn(player2, dyingGuard).getId();
        harness.addToBattlefield(player2, survivingGuard);
        castAngel();
        harness.handleMultipleCardsChosen(player1, List.of(dyingGuard.getId(), survivingGuard.getId()));


        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, dyingPermanentId);
        resolveExileTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(dyingGuard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(survivingGuard).doesNotContain(dyingGuard);
    }

    @Test
    @DisplayName("An opposing hexproof creature cannot be targeted")
    void opposingHexproofCreatureIsNotALegalChoice() {
        RubblebackRhino rhino = new RubblebackRhino();
        harness.addToBattlefield(player2, rhino);
        harness.addToBattlefield(player2, new ArmoryGuard());
        castAngel();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(rhino.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hexproof does not prevent targeting creature cards in a graveyard")
    void hexproofCreatureCardCanBeExiledFromGraveyard() {
        RubblebackRhino rhino = new RubblebackRhino();
        harness.setGraveyard(player2, List.of(rhino));
        castAngel();
        harness.handleMultipleCardsChosen(player1, List.of(rhino.getId()));
        resolveExileTrigger();

        harness.assertNotInGraveyard(player2, "Rubbleback Rhino");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(rhino);
    }
}
