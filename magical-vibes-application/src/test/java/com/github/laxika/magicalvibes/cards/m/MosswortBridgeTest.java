package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MosswortBridgeTest extends BaseCardTest {

    /** Puts Mosswort Bridge on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addBridgeWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new MosswortBridge());
        GameData gd = harness.getGameData();
        Permanent bridge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mosswort Bridge"))
                .findFirst().orElseThrow();
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
}
