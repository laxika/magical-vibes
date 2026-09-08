package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.ForkedBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JubilantSkybonder.class, AirElemental.class, ForkedBolt.class, GrizzlyBears.class,
        LightningBolt.class, ZuranSpellcaster.class})
class JubilantSkybonderTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting a creature with flying costs {2} more")
    void opponentSpellTargetingFlyingCreatureCostsMore() {
        Permanent flyer = addProtectedCreature(new AirElemental());
        prepareOpponentCast(new LightningBolt(), 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");

        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, flyer.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The tax applies once for each flying creature targeted")
    void taxAppliesOnceForEachFlyingCreatureTargeted() {
        Permanent firstFlyer = addProtectedCreature(new AirElemental());
        Permanent secondFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        prepareOpponentCast(new ForkedBolt(), 3);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, null,
                Map.of(firstFlyer.getId(), 1, secondFlyer.getId(), 1),
                List.of(firstFlyer.getId(), secondFlyer.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");

        harness.addMana(player2, ManaColor.RED, 2);
        gs.playCard(gd, player2, 0, 0, null,
                Map.of(firstFlyer.getId(), 1, secondFlyer.getId(), 1),
                List.of(firstFlyer.getId(), secondFlyer.getId()), List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell targeting a nonflying creature is not taxed")
    void nonflyingCreatureIsNotTaxed() {
        harness.addToBattlefield(player1, new JubilantSkybonder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentCast(new LightningBolt(), 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller's spells are not taxed")
    void controllersSpellsAreNotTaxed() {
        Permanent flyer = addProtectedCreature(new AirElemental());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, flyer.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The tax does not apply to opponent activated abilities")
    void opponentActivatedAbilityIsNotTaxed() {
        Permanent flyer = addProtectedCreature(new AirElemental());
        addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2, 0, null, flyer.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addProtectedCreature(Card creature) {
        harness.addToBattlefield(player1, new JubilantSkybonder());
        return harness.addToBattlefieldAndReturn(player1, creature);
    }

    private void prepareOpponentCast(Card spell, int redMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, redMana);
    }
}
