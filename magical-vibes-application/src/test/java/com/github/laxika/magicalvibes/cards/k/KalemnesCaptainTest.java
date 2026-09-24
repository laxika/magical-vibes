package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KalemnesCaptain.class, Bonesplitter.class, GloriousAnthem.class, GrizzlyBears.class})
class KalemnesCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("When Kalemne's Captain becomes monstrous, it exiles all artifacts and enchantments")
    void becomingMonstrousExilesArtifactsAndEnchantments() {
        Permanent captain = addReadyCaptain();
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(captain.isMonstrous()).isTrue();
        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kalemne's Captain cannot activate monstrosity after becoming monstrous")
    void monstrosityOnlyResolvesOnce() {
        addReadyCaptain();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyCaptain() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new KalemnesCaptain());
        captain.setSummoningSick(false);
        return captain;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
