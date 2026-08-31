package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZiatorasEnvoy.class, AirElemental.class, ColossalDreadmaw.class, Forest.class})
class ZiatorasEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller cast a spell with mana value equal to the damage")
    void castsSpellWithManaValueEqualToDamage() {
        AirElemental airElemental = new AirElemental();
        harness.setLibrary(player1, List.of(airElemental));
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack.getLast().getCard()).isSameAs(airElemental);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(airElemental);
    }

    @Test
    @DisplayName("A spell above the damage limit goes directly to hand")
    void putsTooExpensiveSpellIntoHand() {
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(dreadmaw));
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(dreadmaw);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dreadmaw);
    }

    @Test
    @DisplayName("Declining the top-card choice puts the card into hand")
    void decliningPutsCardIntoHand() {
        AirElemental airElemental = new AirElemental();
        harness.setLibrary(player1, List.of(airElemental));
        addAttackingEnvoy();

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(airElemental);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(airElemental);
    }

    @Test
    @DisplayName("Combat damage lets the controller play an eligible land from the top")
    void playsLandFromTop() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    private Permanent addAttackingEnvoy() {
        Permanent envoy = addCreatureReady(player1, new ZiatorasEnvoy());
        envoy.setAttacking(true);
        return envoy;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
