package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.b.BrigidClachansHeart;
import com.github.laxika.magicalvibes.cards.e.EpicStruggle;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BorderPatrol.class, BrigidClachansHeart.class, CephalidConstable.class, EpicStruggle.class, KrosanVerge.class, SuntailHawk.class, TrollAscetic.class})
class CephalidConstableTest extends BaseCardTest {

    @Test
    void selectedTargetsWaitForTheCombatDamageAbilityToResolve() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(hawk.getId())));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hawk);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(hawk);
        assertThat(gd.playerHands.get(player2.getId())).contains(hawk.getOriginalCard());
    }

    @Test
    @DisplayName("Dealing combat damage to player triggers multi-permanent choice")
    void combatDamageTriggersBounce() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns up to the combat damage dealt and can return a noncreature permanent")
    void returnsUpToCombatDamageAndAnyPermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        Permanent verge = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, constable)).isEqualTo(2);

        constable.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(hawk.getId(), verge.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(hawk.getOriginalCard(), verge.getOriginalCard());
    }

    @Test
    @DisplayName("Choosing a permanent to bounce returns it to owner's hand")
    void bouncePermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        UUID hawkId = hawk.getId();

        harness.handleMultiplePermanentsChosen(player1, List.of(hawkId));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInHand(player2, "Suntail Hawk");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Suntail Hawk") && log.contains("returned"));
    }

    @Test
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
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(brigid.getId()));
        resolveAllTriggers();

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
        addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses not to return"));
    }

    @Test
    @DisplayName("No choice when defender has no permanents")
    void noChoiceWhenNoPermanents() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        // player2 has no permanents

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("has no permanents"));
    }

    @Test
    @DisplayName("No trigger when Constable is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot select more permanents than damage dealt")
    void cannotSelectMoreThanDamage() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent hawk1 = addCreatureReady(player2, new SuntailHawk());
        Permanent hawk2 = addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(1);

        List<UUID> allIds = List.of(hawk1.getId(), hawk2.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, allIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Game advances after bounce choice is made")
    void gameAdvancesAfterChoice() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(hawk.getId()));
        resolveAllTriggers();

        // Game should have advanced past combat damage (auto-passes through END_OF_COMBAT)
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Constable")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Only allows choosing permanents controlled by the damaged player")
    void onlyDamagedPlayersPermanentsAreValidTargets() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent attackersPermanent = addCreatureReady(player1, new BorderPatrol());
        Permanent defendersPermanent = addCreatureReady(player2, new BorderPatrol());

        resolveCombat();
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(attackersPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(defendersPermanent.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attackersPermanent);
        assertThat(gd.playerHands.get(player2.getId())).contains(defendersPermanent.getCard());
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
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(patrol1.getId(), patrol2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId).toList())
                .doesNotContain(patrol1.getId(), patrol2.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(patrol1.getCard(), patrol2.getCard());
    }

    @Test
    @DisplayName("Can return a noncreature permanent controlled by the damaged player")
    void canBounceNoncreaturePermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new EpicStruggle());

        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(noncreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId).toList())
                .doesNotContain(noncreature.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(noncreature.getCard());
    }

    @Test
    @DisplayName("An opponent's hexproof permanent cannot be selected for the return ability")
    void cannotBounceOpponentsHexproofPermanent() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent troll = addCreatureReady(player2, new TrollAscetic());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(hawk.getId())
                .doesNotContain(troll.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(troll.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(hawk.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Troll Ascetic");
        harness.assertInHand(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("A permanent controlled by the damaged player returns to its different owner's hand")
    void returnsStolenPermanentToOwnersHand() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        SuntailHawk hawkCard = new SuntailHawk();
        hawkCard.setOwnerId(player1.getId());
        Permanent hawk = addCreatureReady(player2, hawkCard);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(hawk.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(hawk);
        assertThat(gd.playerHands.get(player1.getId())).contains(hawkCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(hawkCard);
    }

    @Test
    @DisplayName("The combat damage trigger still returns a permanent after Constable leaves")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent constable = addCreatureReady(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, constable));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(hawk.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Cephalid Constable");
        harness.assertInHand(player2, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
    }
}
