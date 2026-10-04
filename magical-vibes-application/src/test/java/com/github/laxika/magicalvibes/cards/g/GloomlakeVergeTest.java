package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GloomlakeVerge.class, Island.class, Swamp.class})
class GloomlakeVergeTest extends BaseCardTest {

    @Test
    void addsBlueManaWithoutRestriction() {
        Permanent verge = addCreatureReady(player1, new GloomlakeVerge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    void blackManaAbilityRequiresIslandOrSwamp() {
        Permanent verge = addCreatureReady(player1, new GloomlakeVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Island or a Swamp");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    void addsBlackManaWhenControllingIsland() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new GloomlakeVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void addsBlackManaWhenControllingSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        addCreatureReady(player1, new GloomlakeVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void opponentsIslandDoesNotEnableBlackMana() {
        harness.addToBattlefield(player2, new Island());
        Permanent verge = addCreatureReady(player1, new GloomlakeVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    void tappedIslandStillEnablesBlackMana() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setTapped(true);
        harness.addToBattlefield(player1, new GloomlakeVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingLastQualifyingLandDisablesBlackMana() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new GloomlakeVerge());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        verge.setTapped(false);
        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerGraveyards.get(player1.getId()).add(island.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Island or a Swamp");
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void anotherVergeDoesNotEnableBlackMana() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new GloomlakeVerge());
        harness.addToBattlefield(player1, new GloomlakeVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Island or a Swamp");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    void opponentsSwampDoesNotEnableBlackMana() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new GloomlakeVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }
}
