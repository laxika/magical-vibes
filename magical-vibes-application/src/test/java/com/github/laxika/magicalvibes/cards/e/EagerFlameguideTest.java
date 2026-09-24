package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EagerFlameguide.class, GrizzlyBears.class, Shock.class})
class EagerFlameguideTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB adds three mana restricted to creature spells")
    void etbAddsCreatureSpellOnlyMana() {
        harness.setHand(player1, List.of(new EagerFlameguide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(3);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its death exiles the top two cards and allows casting exiled creatures")
    void deathExilesTopTwoAndAllowsCreatureSpells() {
        Card exiledCreature = new GrizzlyBears();
        Card exiledNoncreature = new Shock();
        harness.setLibrary(player1, List.of(exiledCreature, exiledNoncreature));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithShock(source);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(exiledCreature, exiledNoncreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledNoncreature.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void destroyWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
