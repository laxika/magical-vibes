package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirkoVoskMindDrinker.class, DimirGuildgate.class, WindDrake.class})
class MirkoVoskMindDrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player reveals until four lands and mills every revealed card")
    void combatDamageMillsUntilFourLands() {
        addAttackingMirko(player1);
        harness.setLibrary(player2, List.of(
                new DimirGuildgate(),        // land 1
                new WindDrake(),
                new DimirGuildgate(),        // land 2
                new MirkoVoskMindDrinker(),
                new DimirGuildgate(),        // land 3
                new DimirGuildgate(),        // land 4 -> stop
                new WindDrake()   // stays in library
        ));

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Dimir Guildgate", "Dimir Guildgate", "Dimir Guildgate", "Dimir Guildgate", "Wind Drake", "Mirko Vosk, Mind Drinker");
    }

    @Test
    @DisplayName("A library with fewer than four lands is entirely milled")
    void millsEntireLibraryWhenFewerThanFourLands() {
        addAttackingMirko(player1);
        harness.setLibrary(player2, List.of(new DimirGuildgate(), new WindDrake(), new DimirGuildgate()));

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Dimir Guildgate", "Dimir Guildgate", "Wind Drake");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when Mirko Vosk is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingMirko(player1);
        Permanent blocker = addCreatureReady(player2, new WindDrake());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new DimirGuildgate(), new DimirGuildgate(), new DimirGuildgate(), new DimirGuildgate()));

        resolveCombatAndTrigger();

        // Only the dead blocker hit the graveyard — no cards were revealed or milled.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactly("Wind Drake");
        // The library is untouched apart from the normal draw as the turn passes.
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name")
                .containsOnly("Dimir Guildgate").hasSize(3);
    }

    private Permanent addAttackingMirko(Player player) {
        Permanent mirko = addCreatureReady(player, new MirkoVoskMindDrinker());
        mirko.setAttacking(true);
        return mirko;
    }

    @Test
    @DisplayName("A library containing no lands is entirely put into the graveyard")
    void revealsEntireLandlessLibrary() {
        addAttackingMirko(player1);
        harness.setLibrary(player2, List.of(new WindDrake(), new MirkoVoskMindDrinker()));

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactlyInAnyOrder("Wind Drake", "Mirko Vosk, Mind Drinker");
    }

    @Test
    @DisplayName("The combat damage trigger resolves after Mirko Vosk leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent mirko = addAttackingMirko(player1);
        harness.setLibrary(player2, List.of(new DimirGuildgate(), new WindDrake()));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(mirko);
        gd.playerGraveyards.get(player1.getId()).add(mirko.getCard());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactlyInAnyOrder("Dimir Guildgate", "Wind Drake");
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
