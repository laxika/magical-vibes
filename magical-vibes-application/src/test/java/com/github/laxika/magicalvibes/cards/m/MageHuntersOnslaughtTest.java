package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.p.ProfessorOnyx;
import com.github.laxika.magicalvibes.cards.f.Flunk;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MageHuntersOnslaught.class, CampusGuide.class, ProfessorOnyx.class, Plains.class, Flunk.class})
class MageHuntersOnslaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and makes its block trigger cause life loss")
    void destroysCreatureAndPunishesBlocking() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        Permanent attacker = addCreatureReady(player1, new CampusGuide());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CampusGuide());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Destroys a planeswalker")
    void destroysPlaneswalker() {
        Permanent planeswalker = addReadyPlaneswalker(player2);

        cast(planeswalker);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(planeswalker.getCard().getId()));
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent land = addCreatureReady(player2, new Plains());
        harness.setHand(player1, List.of(new MageHuntersOnslaught()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each blocking creature causes a separate life loss")
    void multipleBlockersEachCauseLifeLoss() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        Permanent attacker = addCreatureReady(player1, new CampusGuide());
        attacker.setAttacking(true);
        Permanent first = addCreatureReady(player2, new CampusGuide());
        Permanent second = addCreatureReady(player2, new CampusGuide());
        cast(target);

        declareBlocks(player1, player2, attacker, first, second);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The spell controller also loses life for blocking")
    void punishesSpellControllersBlocker() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        Permanent attacker = addCreatureReady(player2, new CampusGuide());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new CampusGuide());
        cast(target);

        declareBlocks(player2, player1, attacker, blocker);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life loss uses the last known controller when the blocker dies in response")
    void blockerLeavingDoesNotPreventLifeLoss() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        Permanent attacker = addCreatureReady(player1, new CampusGuide());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CampusGuide());
        cast(target);
        declareBlocks(player1, player2, attacker, blocker);

        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Flunk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An illegal target at resolution prevents the blocking ability from being created")
    void illegalTargetPreventsDelayedTrigger() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        Permanent attacker = addCreatureReady(player1, new CampusGuide());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CampusGuide());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MageHuntersOnslaught(), new Flunk()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        declareBlocks(player1, player2, attacker, blocker);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blocking in a later turn does not cause life loss")
    void blockingAbilityExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        cast(target);
        harness.setLibrary(player2, List.of(new Plains()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent attacker = addCreatureReady(player2, new CampusGuide());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new CampusGuide());

        declareBlocks(player2, player1, attacker, blocker);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void declareBlocks(Player attackingPlayer, Player defendingPlayer,
                               Permanent attacker, Permanent... blockers) {
        prepareDeclareBlockers(attackingPlayer);
        gs.declareBlockers(gd, defendingPlayer, java.util.Arrays.stream(blockers)
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(defendingPlayer.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker)))
                .toList());
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new MageHuntersOnslaught()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ProfessorOnyx());
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        return permanent;
    }

}
