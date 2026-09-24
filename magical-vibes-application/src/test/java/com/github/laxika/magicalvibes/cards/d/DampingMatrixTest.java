package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AuriokTransfixer;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.s.StalkingStones;
import com.github.laxika.magicalvibes.cards.t.TalismanOfUnity;
import com.github.laxika.magicalvibes.cards.v.ViridianJoiner;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        DampingMatrix.class,
        IcyManipulator.class,
        AuriokTransfixer.class,
        BottleGnomes.class,
        CopperMyr.class,
        ViridianJoiner.class,
        TalismanOfUnity.class,
        StalkingStones.class
})
class DampingMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks non-mana activated abilities of artifacts")
    void blocksArtifactActivatedAbilities() {
        Permanent dampingMatrix = addDampingMatrix(player1);
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, dampingMatrix.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Damping Matrix");
    }

    @Test
    @DisplayName("Blocks non-mana activated abilities of creatures")
    void blocksCreatureActivatedAbilities() {
        Permanent dampingMatrix = addDampingMatrix(player1);
        addCreatureReady(player2, new AuriokTransfixer());
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, dampingMatrix.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Damping Matrix");
    }

    @Test
    @DisplayName("Blocks non-mana activated abilities of artifact creatures")
    void blocksArtifactCreatureActivatedAbilities() {
        addDampingMatrix(player1);
        harness.addToBattlefield(player2, new BottleGnomes());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Damping Matrix");
    }

    @Test
    @DisplayName("Does not block artifact mana abilities")
    void allowsArtifactManaAbilities() {
        addDampingMatrix(player1);
        addCreatureReady(player2, new CopperMyr());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not block creature mana abilities")
    void allowsCreatureManaAbilities() {
        addDampingMatrix(player1);
        addCreatureReady(player2, new ViridianJoiner());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not block directly activated artifact mana abilities")
    void allowsDirectlyActivatedArtifactManaAbilities() {
        addDampingMatrix(player1);
        harness.addToBattlefield(player2, new TalismanOfUnity());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not block activated abilities of permanents that are neither artifacts nor creatures")
    void allowsOtherPermanentActivatedAbilities() {
        addDampingMatrix(player1);
        harness.addToBattlefield(player2, new StalkingStones());
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addDampingMatrix(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DampingMatrix());
    }
}
