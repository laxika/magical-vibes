package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AbunaAcolyte;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpittingDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KatabaticWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creature cannot attack while Katabatic Winds is on the battlefield")
    void flyingCreatureCannotAttack() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent flyer = addReadyCreature(player1, true);

        beginAttack(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(idx)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-flying creature attacks normally while Katabatic Winds is on the battlefield")
    void nonFlyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        harness.setLife(player2, 20);
        Permanent ground = addReadyCreature(player1, false);

        beginAttack(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ground);
        gs.declareAttackers(gd, player1, List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Flying creature cannot block while Katabatic Winds is on the battlefield")
    void flyingCreatureCannotBlock() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent attacker = addReadyCreature(player1, false);
        attacker.setAttacking(true);
        addReadyCreature(player2, true);

        beginBlock();

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-flying creature blocks normally while Katabatic Winds is on the battlefield")
    void nonFlyingCreatureCanBlock() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent attacker = addReadyCreature(player1, false);
        attacker.setAttacking(true);
        addReadyCreature(player2, false);

        beginBlock();
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Flying creature cannot activate a {T} ability")
    void flyingCreatureCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent birds = new Permanent(new BirdsOfParadise());
        birds.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(birds);
        int birdsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(birds);

        assertThatThrownBy(() -> harness.activateAbility(player1, birdsIdx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Flying creature can still activate a non-tap ability")
    void flyingCreatureCanActivateNonTapAbility() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent drake = new Permanent(new SpittingDrake());
        drake.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(drake);
        int drakeIdx = gd.playerBattlefields.get(player1.getId()).indexOf(drake);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, drakeIdx, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-flying creature can still activate a {T} ability")
    void nonFlyingCreatureCanActivateTapAbility() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent acolyte = new Permanent(new AbunaAcolyte());
        acolyte.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(acolyte);
        int acolyteIdx = gd.playerBattlefields.get(player1.getId()).indexOf(acolyte);
        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, acolyteIdx, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restriction lifts when Katabatic Winds leaves the battlefield")
    void restrictionLiftsWhenKatabaticWindsLeaves() {
        Permanent winds = new Permanent(new KatabaticWinds());
        gd.playerBattlefields.get(player1.getId()).add(winds);
        harness.setLife(player2, 20);
        Permanent flyer = addReadyCreature(player1, true);

        gd.playerBattlefields.get(player1.getId()).remove(winds);

        beginAttack(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);
        gs.declareAttackers(gd, player1, List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCreature(Player player, boolean flying) {
        Card card = new Card();
        card.setName(flying ? "Test Flyer" : "Test Grounder");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setPower(2);
        card.setToughness(2);
        if (flying) {
            card.setKeywords(EnumSet.of(Keyword.FLYING));
        }
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void beginBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
