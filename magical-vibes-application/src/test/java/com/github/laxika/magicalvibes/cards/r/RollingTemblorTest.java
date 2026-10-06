package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RollingTemblor.class, WalkingCorpse.class, ChapelGeist.class, FortressCrab.class})
class RollingTemblorTest extends BaseCardTest {

    @Test
    @DisplayName("Marks exactly two damage on ground creatures and no damage on flyers")
    void marksDamageOnlyOnGroundCreatures() {
        harness.addToBattlefield(player1, new FortressCrab());
        harness.addToBattlefield(player2, new FortressCrab());
        harness.addToBattlefield(player2, new ChapelGeist());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Flashback requires two red mana even with six mana available")
    void flashbackRequiresTwoRedMana() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rolling Temblor");
    }

    @Test
    @DisplayName("Kills creatures without flying on both sides")
    void killsCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageCreaturesWithFlying() {
        harness.addToBattlefield(player2, new ChapelGeist());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Chapel Geist");
    }

    @Test
    @DisplayName("Damages ground creatures but leaves flyers unharmed")
    void selectivelyDamages() {
        harness.addToBattlefield(player2, new ChapelGeist());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Chapel Geist");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Goes to graveyard after normal cast resolves")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rolling Temblor");
    }

    @Test
    @DisplayName("Flashback kills creatures without flying")
    void flashbackKillsGroundCreatures() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new ChapelGeist());
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Chapel Geist");
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Rolling Temblor");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rolling Temblor"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack with flashback flag")
    void flashbackPutsOnStack() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Rolling Temblor");
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
