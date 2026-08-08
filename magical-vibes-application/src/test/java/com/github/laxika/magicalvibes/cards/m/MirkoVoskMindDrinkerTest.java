package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirkoVoskMindDrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player reveals until four lands and mills every revealed card")
    void combatDamageMillsUntilFourLands() {
        addAttackingMirko(player1);
        setLibrary(player2, List.of(
                new Forest(),        // land 1
                new GrizzlyBears(),
                new Forest(),        // land 2
                new Divination(),
                new Forest(),        // land 3
                new Forest(),        // land 4 -> stop
                new GrizzlyBears()   // stays in library
        ));

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Forest", "Forest", "Forest", "Forest", "Grizzly Bears", "Divination");
    }

    @Test
    @DisplayName("A library with fewer than four lands is entirely milled")
    void millsEntireLibraryWhenFewerThanFourLands() {
        addAttackingMirko(player1);
        setLibrary(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Forest", "Forest", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when Mirko Vosk is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingMirko(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        resolveCombatAndTrigger();

        // Only the dead blocker hit the graveyard — no cards were revealed or milled.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactly("Grizzly Bears");
        // The library is untouched apart from the normal draw as the turn passes.
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name")
                .containsOnly("Forest").hasSize(3);
    }

    private Permanent addAttackingMirko(Player player) {
        Permanent mirko = addCreatureReady(player, new MirkoVoskMindDrinker());
        mirko.setAttacking(true);
        return mirko;
    }

    private void setLibrary(Player player, List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
