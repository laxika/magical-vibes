package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.s.StreetbreakerWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinStreetHooligan.class, IzzetSignet.class, StreetbreakerWurm.class})
class TinStreetHooliganTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact when green mana was spent to cast it")
    void destroysArtifactWhenGreenManaWasSpent() {
        harness.addToBattlefield(player2, new IzzetSignet());
        castTinStreetHooligan(true, harness.getPermanentId(player2, "Izzet Signet"));

        harness.assertInGraveyard(player2, "Izzet Signet");
    }

    @Test
    @DisplayName("Does not destroy an artifact when green mana was not spent to cast it")
    void doesNotDestroyArtifactWithoutGreenMana() {
        harness.addToBattlefield(player2, new IzzetSignet());
        castTinStreetHooligan(false, harness.getPermanentId(player2, "Izzet Signet"));

        harness.assertOnBattlefield(player2, "Izzet Signet");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not require a target when green mana was not spent to cast it")
    void doesNotRequireTargetWithoutGreenMana() {
        castTinStreetHooligan(false, null);

        harness.assertOnBattlefield(player1, "Tin Street Hooligan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can destroy its controller's artifact when green mana was spent")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new IzzetSignet());
        castTinStreetHooligan(true, harness.getPermanentId(player1, "Izzet Signet"));

        harness.assertInGraveyard(player1, "Izzet Signet");
        harness.assertOnBattlefield(player1, "Tin Street Hooligan");
    }

    @Test
    @DisplayName("Enters normally when green mana was spent but no artifact exists")
    void entersWithoutArtifactsWhenGreenManaWasSpent() {
        castTinStreetHooligan(true, null);

        harness.assertOnBattlefield(player1, "Tin Street Hooligan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when it enters without being cast")
    void doesNotTriggerWithoutBeingCast() {
        harness.addToBattlefield(player2, new IzzetSignet());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.enterBattlefieldAndReturn(player1, new TinStreetHooligan());

        harness.assertOnBattlefield(player1, "Tin Street Hooligan");
        harness.assertOnBattlefield(player2, "Izzet Signet");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonArtifactPermanent() {
        harness.addToBattlefield(player2, new StreetbreakerWurm());
        harness.setHand(player1, List.of(new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, harness.getPermanentId(player2, "Streetbreaker Wurm")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private void castTinStreetHooligan(boolean spendGreenMana, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, spendGreenMana ? ManaColor.GREEN : ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
