package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DranaAndLinvala.class, DrudgeSkeletons.class, LlanowarElves.class, ProdigalPyromancer.class})
class DranaAndLinvalaTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks activated abilities of creatures opponents control")
    void blocksOpponentsCreatureAbilities() {
        addReady(player1, new DranaAndLinvala());
        addReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Drana and Linvala");
    }

    @Test
    @DisplayName("Gains an activated ability from an opponent's creature")
    void gainsOpponentsCreatureAbility() {
        Permanent dranaAndLinvala = addReady(player1, new DranaAndLinvala());
        addReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dranaAndLinvala.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains a creature's tap-for-mana ability")
    void gainsOpponentsCreatureManaAbility() {
        addReady(player1, new DranaAndLinvala());
        addReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain activated abilities from creatures its controller controls")
    void ignoresControllersCreatures() {
        addReady(player1, new DranaAndLinvala());
        addReady(player1, new DrudgeSkeletons());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
