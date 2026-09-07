package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinStreetHooligan.class, JalumTome.class, Mountain.class})
class TinStreetHooliganTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact when green mana was spent to cast it")
    void destroysArtifactWhenGreenManaWasSpent() {
        harness.addToBattlefield(player2, new JalumTome());
        castTinStreetHooligan(true, harness.getPermanentId(player2, "Jalum Tome"));

        harness.assertInGraveyard(player2, "Jalum Tome");
    }

    @Test
    @DisplayName("Does not destroy an artifact when green mana was not spent to cast it")
    void doesNotDestroyArtifactWithoutGreenMana() {
        harness.addToBattlefield(player2, new JalumTome());
        castTinStreetHooligan(false, harness.getPermanentId(player2, "Jalum Tome"));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() instanceof JalumTome);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonArtifactPermanent() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, harness.getPermanentId(player2, "Mountain")))
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
