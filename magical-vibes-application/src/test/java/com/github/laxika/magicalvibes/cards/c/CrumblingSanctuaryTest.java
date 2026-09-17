package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.cards.j.JhessianThief;
import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrumblingSanctuary.class, ShockTroops.class, GerrardsIrregulars.class,
        VampireNighthawk.class, JhessianThief.class})
class CrumblingSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage is replaced by exiling cards from the damaged player's library")
    void noncombatDamageExilesLibraryCards() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.addToBattlefield(player1, new ShockTroops());
        harness.setLibrary(player1, List.of(new GerrardsIrregulars(), new CrumblingSanctuary()));

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The replacement affects players other than the artifact's controller")
    void replacementAffectsOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.addToBattlefield(player1, new ShockTroops());
        harness.setLibrary(player2, List.of(new GerrardsIrregulars(), new CrumblingSanctuary()));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Damage has no effect when the damaged player's library is empty")
    void emptyLibraryTakesNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.addToBattlefield(player1, new ShockTroops());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A library shorter than the damage event still replaces all damage")
    void shortLibraryTakesNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.addToBattlefield(player1, new ShockTroops());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GerrardsIrregulars()));

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage is replaced by exiling cards from the defending player's library")
    void combatDamageExilesLibraryCards() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setLibrary(player2, List.of(
                new GerrardsIrregulars(), new CrumblingSanctuary(), new GerrardsIrregulars(), new CrumblingSanctuary()));

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GerrardsIrregulars());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Replaced combat damage does not cause lifelink")
    void replacedCombatDamageDoesNotCauseLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setLibrary(player2, List.of(new GerrardsIrregulars(), new CrumblingSanctuary()));

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Replaced combat damage does not trigger combat-damage abilities")
    void replacedCombatDamageDoesNotTriggerCombatDamageAbilities() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GerrardsIrregulars()));
        harness.setLibrary(player2, List.of(new GerrardsIrregulars()));

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new JhessianThief());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }
}
