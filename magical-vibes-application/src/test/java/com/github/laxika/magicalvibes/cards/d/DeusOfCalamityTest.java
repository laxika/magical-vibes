package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeusOfCalamity.class, Forest.class, Mountain.class, GrizzlyBears.class})
class DeusOfCalamityTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Dealing 6 to an opponent prompts to destroy a land that player controls")
    void promptsToDestroyLand() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent mountain = addPermanent(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mountain.getId());
    }

    @Test
    @DisplayName("The chosen land is destroyed and the game advances")
    void destroysChosenLand() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent mountain = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the damaged player's lands are valid targets (not own lands, not creatures)")
    void onlyDamagedPlayersLands() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent ownLand = addPermanent(player1, new Forest());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyLand = addPermanent(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyLand.getId())
                .doesNotContain(ownLand.getId())
                .doesNotContain(enemyCreature.getId());
    }

    @Test
    @DisplayName("No trigger when the damaged player controls no lands")
    void noTriggerWithoutLands() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No trigger when fewer than 6 damage is dealt to the opponent")
    void noTriggerBelowThreshold() {
        DeusOfCalamity card = new DeusOfCalamity();
        card.setPower(5);
        Permanent deus = addCreatureReady(player1, card);
        deus.setAttacking(true);
        addPermanent(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mountain");
    }
}
