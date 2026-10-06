package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
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

@CardUsed({MosswortBridge.class, AvatarOfMight.class, GrizzlyBears.class, Plains.class})
class MosswortBridgeTest extends BaseCardTest {

    /** Puts Mosswort Bridge on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addBridgeWithImprint(Card imprinted) {
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new MosswortBridge());
        GameData gd = harness.getGameData();
        gd.setImprintedCard(bridge.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return bridge;
    }

    @Test
    @DisplayName("Plays the exiled card when controlled creatures have total power 10 or greater")
    void playsExiledCardWithEnoughPower() {
        GrizzlyBears exiled = new GrizzlyBears();
        addBridgeWithImprint(exiled);
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8 power
        harness.addToBattlefield(player1, new GrizzlyBears());  // 2 power -> total 10
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2); // the pre-placed one plus the freshly played exiled copy
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing while controlled creatures have total power below 10")
    void doesNothingWithInsufficientPower() {
        GrizzlyBears exiled = new GrizzlyBears();
        addBridgeWithImprint(exiled);
        harness.addToBattlefield(player1, new GrizzlyBears()); // only 2 power
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability — condition not met

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        GrizzlyBears exiled = new GrizzlyBears();
        addBridgeWithImprint(exiled);
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8 power
        harness.addToBattlefield(player1, new GrizzlyBears());  // 2 power -> total 10
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    void hideawayExilesOneOfTopFourFaceDownAndBottomsTheRest() {
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        Plains fourth = new Plains();
        Plains fifth = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new MosswortBridge()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        Permanent bridge = findPermanent(player1, "Mosswort Bridge");
        assertThat(bridge.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        GameData gd = harness.getGameData();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(bridge.getCard())).isSameAs(second);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(first, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tapsForGreenManaWithoutUsingTheStack() {
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new MosswortBridge());
        harness.forceActivePlayer(player1);

        harness.tapPermanent(player1, 0);

        assertThat(bridge.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(1);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void checksPowerAtResolutionRatherThanActivation() {
        addBridgeWithImprint(new GrizzlyBears());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void opponentsCreaturesDoNotContributeToPowerThreshold() {
        GrizzlyBears exiled = new GrizzlyBears();
        addBridgeWithImprint(exiled);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void playsExiledLandAndUsesLandPlay() {
        Plains plains = new Plains();
        addBridgeWithImprint(plains);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNull();
        assertThat(harness.getGameData().landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayExiledLandAfterUsingLandPlay() {
        Plains plains = new Plains();
        Permanent bridge = addBridgeWithImprint(plains);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Plains()));
        harness.forceActivePlayer(player1);
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(bridge.getCard())).isSameAs(plains);
        assertThat(harness.getGameData().landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayExiledLandDuringOpponentsTurn() {
        Plains plains = new Plains();
        Permanent bridge = addBridgeWithImprint(plains);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(bridge.getCard())).isSameAs(plains);
    }

    @Test
    @CardUsed({StrionicResonator.class})
    void copiedHideawayAllowsPlayingBothExiledCards() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MosswortBridge()));
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

        findPermanent(player1, "Mosswort Bridge").untap();
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
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
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(3);
    }
}
