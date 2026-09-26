package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HisokasDefiance.class, KamiOfOldStone.class, BlessedBreath.class, DevotedRetainer.class})
class HisokasDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a Spirit spell")
    void countersSpiritSpell() {
        KamiOfOldStone kami = new KamiOfOldStone();
        harness.castFromHand(player1, kami, "{3}{W}");

        harness.setHand(player2, List.of(new HisokasDefiance()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, kami.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Kami of Old Stone");
        harness.assertNotOnBattlefield(player1, "Kami of Old Stone");
        harness.assertInGraveyard(player2, "Hisoka's Defiance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters an Arcane spell")
    void countersArcaneSpell() {
        Permanent retainer = harness.addToBattlefieldAndReturn(player1, new DevotedRetainer());
        BlessedBreath breath = new BlessedBreath();
        harness.setHand(player1, List.of(breath));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new HisokasDefiance()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, retainer.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, breath.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Blessed Breath");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a spell that is neither Spirit nor Arcane")
    void cannotTargetOtherSpell() {
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player1, retainer, "{W}");

        harness.setHand(player2, List.of(new HisokasDefiance()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, retainer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
