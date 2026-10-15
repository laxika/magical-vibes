package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BallistaSquad;
import com.github.laxika.magicalvibes.cards.c.ChieftainEnDal;
import com.github.laxika.magicalvibes.cards.r.RamosianSkyMarshal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantVanguard.class, DefiantFalcon.class, RamosianSkyMarshal.class,
        BallistaSquad.class, ChieftainEnDal.class, Daze.class})
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

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chieftain en-Dal");
        harness.assertInGraveyard(player2, "Defiant Vanguard");
        harness.assertNotOnBattlefield(player1, "Chieftain en-Dal");
        harness.assertNotOnBattlefield(player2, "Defiant Vanguard");
    }

    @Test
    @DisplayName("Both creatures remain until the end-of-combat delayed ability resolves")
    void destructionWaitsForDelayedAbilityResolution() {
        Permanent vanguard = addCreatureReady(player2, new DefiantVanguard());
        ChieftainEnDal attackerCard = new ChieftainEnDal();
        attackerCard.setPower(0);
        attackerCard.setToughness(10);
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vanguard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Defiant Vanguard");
        harness.assertInGraveyard(player1, "Chieftain en-Dal");
    }

    @Test
    @DisplayName("The blocked creature is destroyed even if Vanguard dies in combat")
    void delayedDestructionSurvivesVanguardsCombatDeath() {
        addCreatureReady(player2, new DefiantVanguard());
        ChieftainEnDal attackerCard = new ChieftainEnDal();
        attackerCard.setToughness(10);
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player2, "Defiant Vanguard");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chieftain en-Dal");
    }

    @Test
    @DisplayName("The activated ability offers only Rebel permanents with mana value 4 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        Permanent vanguard = addCreatureReady(player1, new DefiantVanguard());
        harness.setLibrary(player1, List.of(
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
    @DisplayName("The activated ability includes a Rebel permanent with mana value exactly 4")
    void searchIncludesRebelPermanentAtManaValueFour() {
        addCreatureReady(player1, new DefiantVanguard());
        harness.setLibrary(player1, List.of(new BallistaSquad(), new RamosianSkyMarshal()));

        activateVanguard();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Ballista Squad");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addCreatureReady(player1, new DefiantVanguard());
        harness.setLibrary(player1, List.of(new DefiantFalcon()));

        activateVanguard();
        harness.handleCardChosen(player1, 0);

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
        harness.setLibrary(player1, List.of(falcon));

        activateVanguard();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).contains(falcon);
        harness.assertNotOnBattlefield(player1, "Defiant Falcon");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addCreatureReady(player1, new DefiantVanguard());
        harness.setLibrary(player1, List.of(new ChieftainEnDal(), new Daze()));

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
