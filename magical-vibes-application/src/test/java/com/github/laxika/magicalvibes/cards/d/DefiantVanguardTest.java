package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefiantVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Defiant Vanguard destroys itself and the creature it blocked at end of combat")
    void destroysItselfAndBlockedCreatureAtEndOfCombat() {
        Permanent vanguard = addReadyVanguard(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vanguard);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vanguard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Defiant Vanguard");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Defiant Vanguard");
    }

    @Test
    @DisplayName("The activated ability offers only Rebel permanents with mana value 4 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyVanguard(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new DefiantVanguard(),
                new DefiantFalcon(),
                new GiantSpider(),
                new HolyDay()));

        activateVanguard();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Defiant Vanguard", "Defiant Falcon");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyVanguard(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DefiantFalcon());

        activateVanguard();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Defiant Vanguard", "Defiant Falcon");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addReadyVanguard(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GiantSpider(), new HolyDay()));

        activateVanguard();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Defiant Vanguard");
    }

    private Permanent addReadyVanguard(com.github.laxika.magicalvibes.model.Player player) {
        Permanent vanguard = new Permanent(new DefiantVanguard());
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vanguard);
        return vanguard;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        GiantSpider card = new GiantSpider();
        card.setPower(0);
        card.setToughness(10);
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void activateVanguard() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
