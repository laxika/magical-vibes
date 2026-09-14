package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CinderElemental;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaberAnts.class, ShockTroops.class, FreshVolunteers.class, CinderElemental.class})
class SaberAntsTest extends BaseCardTest {

    @Test
    @DisplayName("Taking 2 damage offers and accepting creates two Insect tokens")
    void acceptingDamageTriggerCreatesThatManyTokens() {
        Permanent ants = addCreatureReady(player2, new SaberAnts());
        addCreatureReady(player1, new ShockTroops());

        harness.activateAbility(player1, 0, null, ants.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Insect"))
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Declining the damage trigger creates no Insect tokens")
    void decliningDamageTriggerCreatesNoTokens() {
        Permanent ants = addCreatureReady(player2, new SaberAnts());
        addCreatureReady(player1, new ShockTroops());

        harness.activateAbility(player1, 0, null, ants.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player2, "Insect")).isEmpty();
        assertThat(findPermanents(player2, "Saber Ants")).hasSize(1);
    }

    @Test
    @DisplayName("Taking 1 damage and accepting creates one Insect token")
    void oneDamageCreatesOneToken() {
        Permanent ants = addCreatureReady(player2, new SaberAnts());
        addCreatureReady(player1, new CinderElemental());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, ants.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Insect")).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage also uses the amount of damage dealt")
    void combatDamageCreatesThatManyTokens() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);
        Permanent ants = addCreatureReady(player2, new SaberAnts());
        ants.setBlocking(true);
        ants.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Insect")).hasSize(2);
    }
}
