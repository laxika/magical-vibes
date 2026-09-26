package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerPack.class, Divination.class, GrizzlyBears.class})
class PowerPackTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles a random instant or sorcery for the next upkeep")
    void combatDamageQueuesNextUpkeepFreeCast() {
        Divination divination = new Divination();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, divination));

        Permanent powerPack = addAttackingPowerPack();
        resolveCombat();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(powerPack.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The next upkeep offers the exiled spell and exiles it after resolution")
    void castsExiledSpellAtNextUpkeep() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        addAttackingPowerPack();
        resolveCombat();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(divination);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    @DisplayName("Combat damage does nothing without an instant or sorcery in the graveyard")
    void combatDamageWithNoMatchingCardDoesNothing() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        addAttackingPowerPack();
        resolveCombat();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private Permanent addAttackingPowerPack() {
        Permanent powerPack = new Permanent(new PowerPack());
        powerPack.setSummoningSick(false);
        powerPack.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(powerPack);
        return powerPack;
    }
}
