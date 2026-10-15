package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CallTheMountainChocobo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadesSorcererOfEld;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmetSelchUnsundered.class, HadesSorcererOfEld.class, Forest.class,
        GrizzlyBears.class, LightningBolt.class, Shock.class, CallTheMountainChocobo.class})
class EmetSelchUnsunderedTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws a card and then discards a card")
    void entersAndLoots() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new EmetSelchUnsundered(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking draws a card and then discards a card")
    void attacksAndLoots() {
        Permanent emet = addEmetReady(player1);
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(indexOf(player1, emet)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fourteen cards in the graveyard allow the upkeep transformation")
    void transformsAtFourteenCards() {
        Permanent emet = addEmetReady(player1);
        harness.setGraveyard(player1, filler(14));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(emet.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The upkeep transformation is optional and needs fourteen cards")
    void upkeepTransformationIsOptional() {
        Permanent emet = addEmetReady(player1);
        harness.setGraveyard(player1, filler(14));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(emet.isTransformed()).isFalse();

        harness.setGraveyard(player1, filler(13));
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(emet.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Hades lets its controller play a land and cast a spell from the graveyard on their turn")
    void playsCardsFromGraveyardOnControllerTurn() {
        addHadesReady(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Hades does not grant graveyard play permission during an opponent's turn")
    void graveyardPlayPermissionIsLimitedToControllerTurn() {
        addHadesReady(player1);
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Cards that would enter its controller's graveyard are exiled")
    void exilesOwnCardsInsteadOfGraveyard() {
        addHadesReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The upkeep condition is checked again when the ability resolves")
    void doesNotTransformIfGraveyardFallsBelowFourteen() {
        Permanent emet = addEmetReady(player1);
        harness.setGraveyard(player1, filler(14));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, filler(13));
        harness.passBothPriorities();

        assertThat(emet.isTransformed()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's graveyard does not satisfy the upkeep condition")
    void countsOnlyControllersGraveyard() {
        Permanent emet = addEmetReady(player1);
        harness.setGraveyard(player1, filler(13));
        harness.setGraveyard(player2, filler(14));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(emet.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Hades allows casting permanent spells for their normal costs")
    void castsCreatureFromGraveyard() {
        addHadesReady(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Hades does not grant additional land plays")
    void graveyardLandUsesNormalLandAllowance() {
        addHadesReady(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Hades does not exile cards going to an opponent's graveyard")
    void doesNotReplaceOpponentGraveyardEntries() {
        addHadesReady(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Hades exiles itself rather than dying")
    void exilesItselfWhenLethallyDamaged() {
        Permanent hades = addHadesReady(player1);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, hades.getId());
        harness.castAndResolveInstant(player1, 0, hades.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hades);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof EmetSelchUnsundered);
    }

    @Test
    @DisplayName("Hades exiles tokens instead of allowing them to enter the graveyard")
    void tokensDoNotDieUnderHades() {
        addHadesReady(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CallTheMountainChocobo(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent bird = findPermanent(player1, "Bird");
        int graveyardEntries = gd.permanentsPutIntoGraveyardFromBattlefieldThisTurn;

        harness.castAndResolveInstant(player1, 0, bird.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bird);
        assertThat(gd.permanentsPutIntoGraveyardFromBattlefieldThisTurn)
                .isEqualTo(graveyardEntries);
    }

    private Permanent addEmetReady(Player player) {
        return addCreatureReady(player, new EmetSelchUnsundered());
    }

    private Permanent addHadesReady(Player player) {
        EmetSelchUnsundered emet = new EmetSelchUnsundered();
        Permanent hades = new Permanent(emet);
        hades.setSummoningSick(false);
        hades.setCard(emet.getBackFaceCard());
        hades.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(hades);
        return hades;
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
