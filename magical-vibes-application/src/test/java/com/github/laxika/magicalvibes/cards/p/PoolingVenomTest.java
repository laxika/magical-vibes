package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoolingVenom.class, Mountain.class, GrizzlyBears.class})
class PoolingVenomTest extends BaseCardTest {

    @Test
    @DisplayName("Pooling Venom resolves attached to a target land")
    void resolvesAttachedToLand() {
        Permanent land = addLand(player1);
        harness.setHand(player1, List.of(new PoolingVenom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PoolingVenom
                        && permanent.isAttached()
                        && land.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Pooling Venom cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addLand(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PoolingVenom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Tapping the enchanted land causes its controller to lose 2 life")
    void tappingEnchantedLandCausesLifeLoss() {
        addLandWithAura(player1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Pooling Venom causes the enchanted land's controller to lose life")
    void lifeLossHitsEnchantedLandController() {
        Permanent land = addLand(player2);
        attachAura(player1, land);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Pooling Venom's ability destroys the enchanted land")
    void abilityDestroysEnchantedLand() {
        Permanent land = addLand(player1);
        attachAura(player1, land);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    private Permanent addLand(Player owner) {
        harness.addToBattlefield(owner, new Mountain());
        List<Permanent> battlefield = gd.playerBattlefields.get(owner.getId());
        return battlefield.get(battlefield.size() - 1);
    }

    private Permanent attachAura(Player auraController, Permanent land) {
        Permanent aura = new Permanent(new PoolingVenom());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private void addLandWithAura(Player owner) {
        Permanent land = addLand(owner);
        attachAura(owner, land);
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
