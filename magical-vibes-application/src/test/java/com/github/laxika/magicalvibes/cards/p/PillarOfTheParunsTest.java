package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillarOfTheParuns.class, AzoriusFirstWing.class, SimicInitiate.class})
class PillarOfTheParunsTest extends BaseCardTest {

    @Test
    void tappingAddsManaRestrictedToMulticoloredSpells() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getMulticoloredSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCastsMulticoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player1, List.of(new AzoriusFirstWing()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void restrictedManaCannotCastMonocoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player1, List.of(new SimicInitiate()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
