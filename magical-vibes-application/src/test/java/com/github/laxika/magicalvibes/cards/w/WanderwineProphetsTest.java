package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WanderwineProphetsTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage trigger presents the may ability choice")
    void combatDamagePresentsMayChoice() {
        Permanent prophets = addReadyCreature(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting and sacrificing a Merfolk grants an extra turn")
    void sacrificeMerfolkGrantsExtraTurn() {
        Permanent prophets = addReadyCreature(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        // Accept the may -> prompted to choose which Merfolk to sacrifice (only Prophets qualifies).
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, prophets.getId());
        harness.passBothPriorities(); // resolve the "take an extra turn" effect

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wanderwine Prophets"));
        assertThat(gd.extraTurns).contains(player1.getId());
    }

    @Test
    @DisplayName("Declining the may ability grants no extra turn and sacrifices nothing")
    void decliningGrantsNoExtraTurn() {
        Permanent prophets = addReadyCreature(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.extraTurns).doesNotContain(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wanderwine Prophets"));
    }
}
