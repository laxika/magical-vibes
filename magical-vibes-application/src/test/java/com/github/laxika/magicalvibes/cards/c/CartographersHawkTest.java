package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CartographersHawk.class, Forest.class, Plains.class})
class CartographersHawkTest extends BaseCardTest {

    @Test
    void combatDamageReturnsHawkAndMayPutPlainsOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .satisfies(land -> assertThat(land.isTapped()).isTrue());
    }

    @Test
    void decliningSearchStillReturnsHawk() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Plains);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof Plains);
    }

    @Test
    void doesNotTriggerWhenDamagedPlayerDoesNotControlMoreLands() {
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hawk);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hawk.getCard());
    }
}
