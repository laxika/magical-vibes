package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrizzledAnglerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability mills two and does not transform without a colorless creature in the graveyard")
    void millsWithoutTransformWhenNoColorlessCreature() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        gd.playerDecks.get(player1.getId()).addFirst(new Cancel());
        gd.playerDecks.get(player1.getId()).addFirst(new Cancel());

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(angler.isTransformed()).isFalse();
        assertThat(angler.getCard().getName()).isEqualTo("Grizzled Angler");
    }

    @Test
    @DisplayName("Tap ability transforms when a colorless creature is already in the graveyard")
    void transformsWhenColorlessCreatureAlreadyInGraveyard() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setGraveyard(player1, List.of(new Memnite()));
        gd.playerDecks.get(player1.getId()).addFirst(new Cancel());
        gd.playerDecks.get(player1.getId()).addFirst(new Cancel());

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        assertThat(angler.getCard().getName()).isEqualTo("Grisly Anglerfish");
        assertThat(gqs.getEffectivePower(gd, angler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Tap ability transforms when milling a colorless creature")
    void transformsWhenMillingColorlessCreature() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        gd.playerDecks.get(player1.getId()).addFirst(new Cancel());
        gd.playerDecks.get(player1.getId()).addFirst(new Memnite());

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        assertThat(angler.getCard().getName()).isEqualTo("Grisly Anglerfish");
    }

    @Test
    @DisplayName("Back face {6} forces opponents' creatures to attack this turn")
    void backFaceForcesOpponentCreaturesToAttack() {
        Permanent anglerfish = createTransformedAnglerfish(player1);
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, indexOf(player1, anglerfish), null, null);
        harness.passBothPriorities();

        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
    }

    private Permanent createTransformedAnglerfish(Player player) {
        Permanent angler = addCreatureReady(player, new GrizzledAngler());
        harness.setGraveyard(player, List.of(new Memnite()));
        gd.playerDecks.get(player.getId()).addFirst(new Cancel());
        gd.playerDecks.get(player.getId()).addFirst(new Cancel());

        harness.activateAbility(player, indexOf(player, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        return angler;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
