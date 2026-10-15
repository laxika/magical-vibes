package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
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

@CardUsed({PoolingVenom.class, DryadArbor.class, Tarmogoyf.class})
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
    @DisplayName("Pooling Venom can enchant an opponent's land")
    void resolvesAttachedToOpponentsLand() {
        Permanent land = addLand(player2);
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Tarmogoyf());
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
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Pooling Venom uses the land's controller when the trigger resolves")
    void lifeLossUsesCurrentEnchantedLandController() {
        Permanent land = addLand(player1);
        attachAura(player1, land);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        resolveAllTriggers();

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

        harness.assertNotOnBattlefield(player1, "Dryad Arbor");
    }

    @Test
    @DisplayName("Pooling Venom's ability destroys an opponent's enchanted land")
    void abilityDestroysOpponentsEnchantedLand() {
        Permanent land = addLand(player2);
        attachAura(player1, land);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
    }

    @Test
    @DisplayName("A pending life-loss trigger still resolves after the enchanted land is destroyed")
    void lifeLossSurvivesEnchantedLandLeavingBattlefield() {
        Permanent land = addLand(player2);
        attachAura(player1, land);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player2, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
        harness.assertInGraveyard(player2, "Dryad Arbor");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Tapping a different land does not trigger Pooling Venom")
    void tappingUnenchantedLandDoesNotCauseLifeLoss() {
        addLandWithAura(player1);
        addLand(player1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Pooling Venom attached to a land triggers separately")
    void multipleAurasEachCauseLifeLoss() {
        Permanent land = addLand(player2);
        attachAura(player1, land);
        attachAura(player1, land);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    private Permanent addLand(Player owner) {
        Permanent land = harness.addToBattlefieldAndReturn(owner, new DryadArbor());
        land.setSummoningSick(false);
        return land;
    }

    private Permanent attachAura(Player auraController, Permanent land) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new PoolingVenom());
        aura.setAttachedTo(land.getId());
        return aura;
    }

    private void addLandWithAura(Player owner) {
        Permanent land = addLand(owner);
        attachAura(owner, land);
    }
}
