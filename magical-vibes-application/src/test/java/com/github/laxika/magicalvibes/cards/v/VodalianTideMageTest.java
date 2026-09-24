package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VodalianTideMage.class, GrizzlyBears.class, ShivanDragon.class})
class VodalianTideMageTest extends BaseCardTest {

    @Test
    void conjuresDuplicateOfTheOnlyOtherNontokenCreature() {
        Permanent mage = addCreatureReady(player1, new VodalianTideMage());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mage, attacker);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears") && card.isTokenCard());
    }

    @Test
    void choosesOneOfSeveralEligibleCombatDamageDealers() {
        addCreatureReady(player1, new VodalianTideMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        bears.setAttacking(true);
        dragon.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), dragon.getId());

        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shivan Dragon") && card.isTokenCard());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears") && card.isTokenCard());
    }

    @Test
    void ignoresTheMageItselfAndTokenCombatDamage() {
        Permanent mage = addCreatureReady(player1, new VodalianTideMage());
        mage.setAttacking(true);
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        tokenCard.setTokenCard(true);
        Permanent token = addCreatureReady(player1, tokenCard);
        token.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears") && card.isTokenCard());
    }
}
