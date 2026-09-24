package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiligreeAttendant.class, Spellbook.class, GrizzlyBears.class})
class FiligreeAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts its controller controls")
    void powerEqualsControlledArtifacts() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(3);
    }
}
