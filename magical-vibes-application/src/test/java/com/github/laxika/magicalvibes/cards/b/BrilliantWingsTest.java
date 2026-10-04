package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrilliantWings.class, GrizzlyBears.class, FountainOfYouth.class})
class BrilliantWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has flying and hexproof")
    void enchantedCreatureHasFlyingAndHexproof() {
        Permanent bears = addReadyCreature(player1);
        addAttachedWings(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Paying {1} attaches Brilliant Wings to an entering creature you control")
    void payingOneManaAttachesToEnteringCreature() {
        Permanent original = addReadyCreature(player1);
        Permanent wings = addAttachedWings(original);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != original
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(wings.getAttachedTo()).isEqualTo(entering.getId());
    }

    @Test
    @DisplayName("Declining the payment leaves Brilliant Wings attached to its original creature")
    void decliningPaymentLeavesAuraAttached() {
        Permanent original = addReadyCreature(player1);
        Permanent wings = addAttachedWings(original);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wings.getAttachedTo()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("Brilliant Wings cannot enchant an opponent's creature")
    void cannotEnchantOpponentsCreature() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new BrilliantWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttachedWings(Permanent host) {
        Permanent wings = new Permanent(new BrilliantWings());
        wings.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(wings);
        return wings;
    }
}
