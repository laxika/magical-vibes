package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemorialToWarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Memorial to War has correct card structure")
    void hasCorrectProperties() {
        MemorialToWar card = new MemorialToWar();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(EntersTappedEffect.class);
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var sacrificeAbility = card.getActivatedAbilities().get(0);
        assertThat(sacrificeAbility.isRequiresTap()).isTrue();
        assertThat(sacrificeAbility.getManaCost()).isEqualTo("{4}{R}");
        assertThat(sacrificeAbility.getEffects()).hasSize(2);
        assertThat(sacrificeAbility.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(sacrificeAbility.getEffects().get(1)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(sacrificeAbility.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter = (PermanentPredicateTargetFilter) sacrificeAbility.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentIsLandPredicate.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Memorial to War enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new MemorialToWar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent memorial = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Memorial to War"))
                .findFirst().orElseThrow();
        assertThat(memorial.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Memorial to War produces red mana")
    void tappingProducesRedMana() {
        Permanent memorial = addMemorialReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(memorial);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack")
    void sacrificeAbilityPutsOnStack() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Memorial to War");
    }

    @Test
    @DisplayName("Memorial is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Memorial to War"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memorial to War"));
    }

    @Test
    @DisplayName("Resolving sacrifice ability destroys target land")
    void resolvingSacrificeAbilityDestroysTargetLand() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent memorial = addMemorialReady(player1);
        memorial.tap();
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a creature with sacrifice ability")
    void cannotTargetCreature() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target land is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helper methods =====

    private Permanent addMemorialReady(Player player) {
        MemorialToWar card = new MemorialToWar();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
