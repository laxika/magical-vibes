package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawningPurist.class, GloriousAnthem.class, GrizzlyBears.class})
class DawningPuristTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the combat damage trigger destroys an enchantment controlled by the damaged player")
    void destroysDamagedPlayersEnchantment() {
        attackWithPurist();
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(anthem.getId()));

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Declining the combat damage trigger leaves the enchantment on the battlefield")
    void declineLeavesEnchantment() {
        attackWithPurist();
        harness.addToBattlefield(player2, new GloriousAnthem());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Only enchantments controlled by the damaged player are legal choices")
    void onlyDamagedPlayersEnchantmentsAreChoices() {
        attackWithPurist();
        Permanent ownAnthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent enemyAnthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent enemyCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(enemyAnthem.getId())
                .doesNotContain(ownAnthem.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("Dawning Purist can be cast face down and turned face up for its morph cost")
    void canBeMorphed() {
        harness.setHand(player1, List.of(new DawningPurist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent purist = findPermanent(player1, "Dawning Purist");
        assertThat(purist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int puristIndex = gd.playerBattlefields.get(player1.getId()).indexOf(purist);
        harness.turnFaceUp(player1, puristIndex);
        harness.passBothPriorities();

        assertThat(purist.isFaceDown()).isFalse();
    }

    private void attackWithPurist() {
        Permanent purist = addCreatureReady(player1, new DawningPurist());
        purist.setAttacking(true);
    }
}
