package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpinerockKnollTest extends BaseCardTest {

    /** Puts Spinerock Knoll on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addKnollWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new SpinerockKnoll());
        GameData gd = harness.getGameData();
        Permanent knoll = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spinerock Knoll"))
                .findFirst().orElseThrow();
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
