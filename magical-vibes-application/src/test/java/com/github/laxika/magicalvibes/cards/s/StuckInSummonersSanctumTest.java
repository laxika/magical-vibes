package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindStone;
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

@CardUsed({StuckInSummonersSanctum.class, Forest.class, MindStone.class, BottleGnomes.class})
class StuckInSummonersSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to and taps an artifact")
    void entersAttachedAndTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.setHand(player1, List.of(new StuckInSummonersSanctum()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Enchanted permanent does not untap during its controller's untap step")
    void enchantedPermanentDoesNotUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        artifact.tap();
        attachAura(artifact, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature's activated abilities cannot be activated")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        creature.setSummoningSick(false);
        attachAura(creature, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted artifact's mana abilities cannot be activated")
    void enchantedArtifactCannotActivateManaAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        attachAura(artifact, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StuckInSummonersSanctum()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Removing the Aura allows the enchanted permanent to untap")
    void removingAuraRestoresUntapping() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        artifact.tap();
        Permanent aura = attachAura(artifact, player2);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        advanceToNextTurn(player2);

        assertThat(artifact.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent attachAura(Permanent target, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new StuckInSummonersSanctum());
        aura.setAttachedTo(target.getId());
        return aura;
    }
}
