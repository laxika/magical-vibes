package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenNote.class, GrizzlyBears.class, Mountain.class})
class MoltenNoteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to total mana spent and untaps your creatures")
    void dealsManaSpentDamageAndUntaps() {
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();

        harness.setHand(player1, List.of(new MoltenNote()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, enemy.getId());

        assertThat(enemy.getMarkedDamage()).isEqualTo(5);
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("X zero still deals two damage and untaps only your creatures")
    void zeroXStillCountsColoredManaAndRestrictsUntapScope() {
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyAlly = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAlly = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        enemyAlly.tap();
        ally.tap();
        secondAlly.tap();
        land.tap();
        harness.setHand(player1, List.of(new MoltenNote()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0, enemy.getId());

        assertThat(enemy.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemy);
        assertThat(ally.isTapped()).isFalse();
        assertThat(secondAlly.isTapped()).isFalse();
        assertThat(enemyAlly.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Molten Note");
    }

    @Test
    @DisplayName("Flashback deals eight damage, untaps creatures, and exiles the spell")
    void flashbackCountsItsFixedManaPayment() {
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();
        MoltenNote spell = new MoltenNote();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveFlashback(player1, 0, enemy.getId());

        assertThat(enemy.getMarkedDamage()).isEqualTo(8);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemy);
        assertThat(ally.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Molten Note");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("An invalidated sole target prevents the untap")
    void illegalTargetPreventsUntapping() {
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();
        harness.setHand(player1, List.of(new MoltenNote()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0, enemy.getId());
        gd.playerBattlefields.get(player2.getId()).remove(enemy);
        gd.playerGraveyards.get(player2.getId()).add(enemy.getCard());

        harness.passBothPriorities();

        assertThat(ally.isTapped()).isTrue();
        assertThat(enemy.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Molten Note");
    }

    @Test
    @DisplayName("Flashback is exiled even when its sole target becomes illegal")
    void illegalFlashbackTargetStillExilesSpell() {
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();
        MoltenNote spell = new MoltenNote();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castFlashback(player1, 0, enemy.getId());
        gd.playerBattlefields.get(player2.getId()).remove(enemy);
        gd.playerGraveyards.get(player2.getId()).add(enemy.getCard());

        harness.passBothPriorities();

        assertThat(ally.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Molten Note");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Noncreature lands are not legal targets")
    void rejectsNoncreatureLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new MoltenNote()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Players are not legal targets")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new MoltenNote()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
