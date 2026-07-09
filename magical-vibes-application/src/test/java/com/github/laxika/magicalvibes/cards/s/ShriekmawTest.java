package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShriekmawTest extends BaseCardTest {

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB destroys target nonblack, nonartifact creature and Shriekmaw stays")
    void hardcastDestroysCreatureAndStays() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shriekmaw"));
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying {1}{B}, ETB destroys the target and Shriekmaw is sacrificed")
    void evokeDestroysAndSacrificesSelf() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger (destroy + evoke sacrifice)

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shriekmaw"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shriekmaw"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Illegal targets =====

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new RavenousRats());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID blackId = harness.getPermanentId(player2, "Ravenous Rats");
        assertThatThrownBy(() ->
                harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, blackId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        assertThatThrownBy(() ->
                harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, artifactId, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
