package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChieftainEnDal;
import com.github.laxika.magicalvibes.cards.r.RamosianSkyMarshal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantVanguard.class, DefiantFalcon.class, RamosianSkyMarshal.class,
        ChieftainEnDal.class, Daze.class})
class DefiantVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Defiant Vanguard destroys itself and the creature it blocked at end of combat")
    void destroysItselfAndBlockedCreatureAtEndOfCombat() {
        Permanent vanguard = addCreatureReady(player2, new DefiantVanguard());
        ChieftainEnDal attackerCard = new ChieftainEnDal();
        attackerCard.setPower(0);
        attackerCard.setToughness(10);
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vanguard);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vanguard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chieftain en-Dal");
        harness.assertInGraveyard(player2, "Defiant Vanguard");
        harness.assertNotOnBattlefield(player1, "Chieftain en-Dal");
        harness.assertNotOnBattlefield(player2, "Defiant Vanguard");
    }

    @Test
    @DisplayName("The activated ability offers only Rebel permanents with mana value 4 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        Permanent vanguard = addCreatureReady(player1, new DefiantVanguard());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new DefiantVanguard(),
                new DefiantFalcon(),
                new RamosianSkyMarshal(),
                new ChieftainEnDal(),
                new Daze()));

        activateVanguard();

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
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
        addCreatureReady(player1, new DefiantVanguard());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DefiantFalcon());

        activateVanguard();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Defiant Vanguard", "Defiant Falcon");
        assertThat(findPermanent(player1, "Defiant Falcon").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability may fail to find a matching Rebel")
    void mayFailToFindMatchingRebel() {
        addCreatureReady(player1, new DefiantVanguard());
        DefiantFalcon falcon = new DefiantFalcon();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(falcon);

        activateVanguard();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).contains(falcon);
        harness.assertNotOnBattlefield(player1, "Defiant Falcon");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addCreatureReady(player1, new DefiantVanguard());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ChieftainEnDal(), new Daze()));

        activateVanguard();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Defiant Vanguard");
    }

    private void activateVanguard() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
