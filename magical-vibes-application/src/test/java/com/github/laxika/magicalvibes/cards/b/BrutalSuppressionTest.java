package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FaultRiders;
import com.github.laxika.magicalvibes.cards.r.RebelInformer;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalSuppression.class, RebelInformer.class, FaultRiders.class, RhysticCave.class})
class BrutalSuppressionTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken Rebel abilities require sacrificing a land")
    void taxesNontokenRebelAbility() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new RebelInformer());
        Permanent target = addCreatureReady(player1, new RebelInformer());
        Permanent land = addLand(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The enchantment's controller also pays the additional land cost")
    void taxesControllerNontokenRebelAbility() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        Permanent rebel = addCreatureReady(player1, new RebelInformer());
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, rebel.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The added land cost can be chosen when multiple lands are available")
    void promptsForLandChoice() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new RebelInformer());
        Permanent target = addCreatureReady(player1, new RebelInformer());
        Permanent firstLand = addLand(player2);
        Permanent secondLand = addLand(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, target.getId());

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player2, secondLand.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstLand).doesNotContain(secondLand);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A nontoken Rebel ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new RebelInformer());
        Permanent target = addCreatureReady(player1, new RebelInformer());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Rebel abilities are not taxed")
    void doesNotTaxNonRebelAbility() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new FaultRiders());
        Permanent land = addLand(player2);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Token Rebels are not taxed")
    void doesNotTaxTokenRebel() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        Permanent target = addCreatureReady(player1, new RebelInformer());
        RebelInformer tokenRebel = new RebelInformer();
        tokenRebel.setToken(true);
        addCreatureReady(player2, tokenRebel);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addLand(com.github.laxika.magicalvibes.model.Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new RhysticCave());
        land.setSummoningSick(false);
        return land;
    }
}
