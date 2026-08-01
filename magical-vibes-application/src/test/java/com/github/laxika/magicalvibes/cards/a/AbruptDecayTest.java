package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbruptDecayTest extends BaseCardTest {

    private void giveManaAndCard(AbruptDecay card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Destroys target nonland permanent with mana value 3 or less")
    void destroysLowManaValuePermanent() {
        // Gray Ogre is {2}{R} — mana value exactly 3 (boundary).
        harness.addToBattlefield(player2, new GrayOgre());
        UUID targetId = harness.getPermanentId(player2, "Gray Ogre");
        giveManaAndCard(new AbruptDecay());

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gray Ogre");
        harness.assertInGraveyard(player2, "Gray Ogre");
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value greater than 3")
    void cannotTargetHighManaValue() {
        // Hill Giant is {3}{R} — mana value 4.
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        giveManaAndCard(new AbruptDecay());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        giveManaAndCard(new AbruptDecay());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Can't be countered — Cancel resolves but Abrupt Decay still destroys")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        AbruptDecay decay = new AbruptDecay();
        giveManaAndCard(decay);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, decay.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }
}
