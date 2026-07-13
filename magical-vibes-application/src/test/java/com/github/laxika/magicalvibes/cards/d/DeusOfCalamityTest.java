package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeusOfCalamityTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Dealing 6 to an opponent prompts to destroy a land that player controls")
    void promptsToDestroyLand() {
        Permanent deus = addReadyCreature(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent mountain = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities(); // resolve destroy trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(mountain.getId());
    }

    @Test
    @DisplayName("The chosen land is destroyed and the game advances")
    void destroysChosenLand() {
        Permanent deus = addReadyCreature(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent mountain = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Only the damaged player's lands are valid targets (not own lands, not creatures)")
    void onlyDamagedPlayersLands() {
        Permanent deus = addReadyCreature(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent ownLand = addPermanent(player1, new Forest());
        Permanent enemyCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent enemyLand = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyLand.getId())
                .doesNotContain(ownLand.getId())
                .doesNotContain(enemyCreature.getId());
    }

    @Test
    @DisplayName("No trigger when the damaged player controls no lands")
    void noTriggerWithoutLands() {
        Permanent deus = addReadyCreature(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No trigger when fewer than 6 damage is dealt to the opponent")
    void noTriggerBelowThreshold() {
        DeusOfCalamity card = new DeusOfCalamity();
        card.setPower(5);
        Permanent deus = addReadyCreature(player1, card);
        deus.setAttacking(true);
        addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mountain"));
    }
}
