package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrigidClachansHeart;
import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.e.EpicStruggle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidConstable.class, BorderPatrol.class, EpicStruggle.class})
class CephalidConstableTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to player triggers multi-permanent choice")
    void combatDamageTriggersBounce() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a permanent to bounce returns it to its owner's hand")
    void bouncePermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        BorderPatrol patrolCard = new BorderPatrol();
        patrolCard.setOwnerId(player1.getId());
        Permanent patrol = harness.addToBattlefieldAndReturn(player2, patrolCard);

        resolveCombat();

        UUID patrolId = patrol.getId();

        harness.handleMultiplePermanentsChosen(player1, List.of(patrolId));

        harness.assertNotOnBattlefield(player2, "Border Patrol");
        harness.assertInHand(player1, "Border Patrol");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Border Patrol") && log.contains("returned"));
    }

    @Test
    @DisplayName("Only allows choosing permanents controlled by the damaged player")
    void onlyDamagedPlayersPermanentsAreValidTargets() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent attackersPermanent = addCreatureReady(player1, new BorderPatrol());
        Permanent defendersPermanent = addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(attackersPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(defendersPermanent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attackersPermanent);
        assertThat(gd.playerHands.get(player2.getId())).contains(defendersPermanent.getCard());
    }

    @Test
    @CardUsed(BrigidClachansHeart.class)
    @DisplayName("Bouncing a transformed permanent returns its physical front-face card")
    void bounceTransformedPermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent brigid = addCreatureReady(player2, new BrigidClachansHeart());
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
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player2, "Border Patrol");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses not to return"));
    }

    @Test
    @DisplayName("No choice when damaged player controls no permanents")
    void noChoiceWhenNoPermanents() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("has no permanents"));
    }

    @Test
    @DisplayName("No trigger when Constable is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BorderPatrol());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot select more permanents than damage dealt")
    void cannotSelectMoreThanDamage() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent patrol1 = addCreatureReady(player2, new BorderPatrol());
        Permanent patrol2 = addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(1);

        List<UUID> allIds = List.of(patrol1.getId(), patrol2.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, allIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Game advances after bounce choice is made")
    void gameAdvancesAfterChoice() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent patrol = addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(patrol.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Allows returning up to the amount of combat damage dealt")
    void scalesMaximumWithCombatDamage() {
        CephalidConstable constableCard = new CephalidConstable();
        constableCard.setPower(2);
        constableCard.setToughness(2);
        Permanent constable = addCreatureReady(player1, constableCard);
        constable.setAttacking(true);
        Permanent patrol1 = addCreatureReady(player2, new BorderPatrol());
        Permanent patrol2 = addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(patrol1.getId(), patrol2.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId).toList())
                .doesNotContain(patrol1.getId(), patrol2.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(patrol1.getCard(), patrol2.getCard());
    }

    @Test
    @DisplayName("Can return a noncreature permanent controlled by the damaged player")
    void canBounceNoncreaturePermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        harness.addToBattlefield(player2, new EpicStruggle());
        Permanent noncreature = findPermanent(player2, "Epic Struggle");

        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(noncreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId).toList())
                .doesNotContain(noncreature.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(noncreature.getCard());
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Constable")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        addCreatureReady(player2, new BorderPatrol());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
