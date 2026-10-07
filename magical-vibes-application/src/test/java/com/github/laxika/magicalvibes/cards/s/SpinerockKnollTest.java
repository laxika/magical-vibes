package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LashOut;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinerockKnoll.class, GrizzlyBears.class, Forest.class, WoodlandChangeling.class,
        LashOut.class})
class SpinerockKnollTest extends BaseCardTest {

    /** Puts Spinerock Knoll on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addKnollWithImprint(Card imprinted) {
        Permanent knoll = harness.addToBattlefieldAndReturn(player1, new SpinerockKnoll());
        GameData gd = harness.getGameData();
        gd.setImprintedCard(knoll.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return knoll;
    }

    @Test
    @DisplayName("Plays the exiled card when an opponent was dealt 7 or more damage this turn")
    void playsExiledCardAfterSevenDamage() {
        GrizzlyBears bears = new GrizzlyBears();
        addKnollWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing while no opponent was dealt 7 damage this turn")
    void doesNothingBelowThreshold() {
        GrizzlyBears bears = new GrizzlyBears();
        addKnollWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.recordDamageToPlayer(player2.getId(), 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability — condition not met

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage dealt to the controller does not satisfy the condition")
    void controllerDamageDoesNotCount() {
        GrizzlyBears bears = new GrizzlyBears();
        addKnollWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.recordDamageToPlayer(player1.getId(), 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        addKnollWithImprint(bears);
        GameData gd = harness.getGameData();
        gd.recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void hideawayExilesOneOfTopFourFaceDownAndBottomsTheRest() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        Forest fifth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new SpinerockKnoll()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        Permanent knoll = findPermanent(player1, "Spinerock Knoll");
        assertThat(knoll.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        GameData gd = harness.getGameData();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(knoll.getCard())).isSameAs(second);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(first, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tapsForRedManaWithoutUsingTheStack() {
        Permanent knoll = harness.addToBattlefieldAndReturn(player1, new SpinerockKnoll());
        harness.forceActivePlayer(player1);

        harness.tapPermanent(player1, 0);

        assertThat(knoll.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED))
                .isEqualTo(1);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void cumulativeDamageIsCheckedAtResolution() {
        addKnollWithImprint(new WoodlandChangeling());
        GameData gd = harness.getGameData();
        gd.recordDamageToPlayer(player2.getId(), 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, null);
        gd.recordDamageToPlayer(player2.getId(), 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    void playsExiledLandAndUsesLandPlay() {
        Forest forest = new Forest();
        addKnollWithImprint(forest);
        harness.getGameData().recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(harness.getGameData().findExiledCard(forest.getId())).isNull();
        assertThat(harness.getGameData().landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayExiledLandAfterUsingLandPlay() {
        Forest forest = new Forest();
        Permanent knoll = addKnollWithImprint(forest);
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.playLand(player1, 0);
        harness.getGameData().recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getGameData().findExiledCard(forest.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(knoll.getCard())).isSameAs(forest);
        assertThat(harness.getGameData().landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayExiledLandDuringOpponentsTurn() {
        Forest forest = new Forest();
        Permanent knoll = addKnollWithImprint(forest);
        harness.forceActivePlayer(player2);
        harness.getGameData().recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(harness.getGameData().findExiledCard(forest.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(knoll.getCard())).isSameAs(forest);
    }

    @Test
    void uncastableExiledSpellRemainsAvailableForLaterActivation() {
        LashOut lashOut = new LashOut();
        Permanent knoll = addKnollWithImprint(lashOut);
        harness.forceActivePlayer(player1);
        harness.getGameData().recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getGameData().findExiledCard(lashOut.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(knoll.getCard())).isSameAs(lashOut);
        knoll.untap();
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @CardUsed({StrionicResonator.class})
    void copiedHideawayAllowsPlayingBothExiledCards() {
        WoodlandChangeling first = new WoodlandChangeling();
        WoodlandChangeling second = new WoodlandChangeling();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SpinerockKnoll()));
        harness.forceActivePlayer(player1);
        harness.playLand(player1, 0);
        harness.addToBattlefield(player1, new StrionicResonator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        var triggerId = harness.getGameData().stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst().orElseThrow().getCard().getId();

        harness.activateAbility(player1, 1, null, triggerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);

        findPermanent(player1, "Spinerock Knoll").untap();
        harness.getGameData().recordDamageToPlayer(player2.getId(), 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();
        if (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Woodland Changeling"))
                .hasSize(2);
    }
}
