package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoltariVisionaryTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("When Soltari Visionary deals damage to a player, it prompts to destroy that player's enchantment")
    void promptsToDestroyDamagedPlayersEnchantment() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent anthem = addPermanent(player2, new GloriousAnthem());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsOnly(anthem.getId());
    }

    @Test
    @DisplayName("The chosen enchantment is destroyed")
    void destroysChosenEnchantment() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent anthem = addPermanent(player2, new GloriousAnthem());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(anthem.getId()));

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Only enchantments controlled by the damaged player can be chosen")
    void onlyDamagedPlayersEnchantments() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent ownAnthem = addPermanent(player1, new GloriousAnthem());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyAnthem = addPermanent(player2, new GloriousAnthem());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsOnly(enemyAnthem.getId())
                .doesNotContain(ownAnthem.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("No destroy choice is created when the damaged player controls no enchantments")
    void noTriggerWithoutEnchantments() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
