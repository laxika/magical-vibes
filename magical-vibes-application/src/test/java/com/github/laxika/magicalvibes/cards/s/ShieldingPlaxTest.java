package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldingPlax.class, GrizzlyBears.class, Shock.class, ProdigalSorcerer.class})
class ShieldingPlaxTest extends BaseCardTest {

    @Test
    @DisplayName("Shielding Plax draws a card when it enters attached to a creature")
    void drawsCardWhenItEnters() {
        Permanent creature = addReadyCreature(player1);
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ShieldingPlax()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(libraryCard.getId()));
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ShieldingPlax
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature can't be targeted by an opponent's spell")
    void opponentSpellCannotTargetEnchantedCreature() {
        Permanent creature = addShieldingPlax(player1, player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Enchanted creature can't be targeted by an opponent's ability")
    void opponentAbilityCannotTargetEnchantedCreature() {
        Permanent creature = addShieldingPlax(player1, player1);
        Permanent sorcerer = addReadySorcerer(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(sorcerer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can still be targeted by its controller's spell")
    void controllerCanTargetEnchantedCreature() {
        Permanent creature = addShieldingPlax(player1, player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    private Permanent addShieldingPlax(Player creatureController, Player auraController) {
        Permanent creature = addReadyCreature(creatureController);
        Permanent aura = new Permanent(new ShieldingPlax());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return creature;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addReadySorcerer(Player player) {
        Permanent sorcerer = new Permanent(new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sorcerer);
        return sorcerer;
    }
}
