package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EchoingCalmTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment and every other enchantment with the same name")
    void destroysTargetAndAllWithSameName() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Leaves enchantments with different names on the battlefield")
    void leavesDifferentNames() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GloriousAnthem());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }
}
