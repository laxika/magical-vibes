package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrigidClachansHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidConstable.class, GrizzlyBears.class})
class CephalidConstableTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage to player triggers multi-permanent choice")
    void combatDamageTriggersBounce() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a permanent to bounce returns it to owner's hand")
    void bouncePermanent() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        UUID bearsId = bears.getId();

        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("returned"));
    }

    @Test
    @CardUsed(BrigidClachansHeart.class)
    @DisplayName("Bouncing a transformed permanent returns its physical front-face card")
    void bounceTransformedPermanent() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent brigid = addReadyCreature(player2, new BrigidClachansHeart());
        Card physicalCard = brigid.getOriginalCard();
        Card backFace = physicalCard.getBackFaceCard();
        brigid.setCard(backFace);
        brigid.setTransformed(true);

        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(brigid.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(brigid);
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(physicalCard)
                .doesNotContain(backFace);
    }

    @Test
    @DisplayName("Choosing zero permanents is allowed (up to)")
    void chooseZeroPermanents() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses not to return"));
    }

    @Test
    @DisplayName("No trigger when defender has no permanents")
    void noTriggerWhenNoPermanents() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        // player2 has no permanents

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("has no permanents"));
    }

    @Test
    @DisplayName("No trigger when Constable is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot select more permanents than damage dealt")
    void cannotSelectMoreThanDamage() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent bears1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent bears2 = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(1);

        List<UUID> allIds = List.of(bears1.getId(), bears2.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, allIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Game advances after bounce choice is made")
    void gameAdvancesAfterChoice() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Game should have advanced past combat damage (auto-passes through END_OF_COMBAT)
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Constable")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}

