package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeaveNoTrace.class, AngelicChorus.class, BadMoon.class, GloriousAnthem.class, GrizzlyBears.class})
class LeaveNoTraceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target and every other enchantment sharing a color with it")
    void destroysTargetAndColorSharingEnchantments() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new BadMoon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeaveNoTrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Bad Moon");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target only an enchantment")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeaveNoTrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
