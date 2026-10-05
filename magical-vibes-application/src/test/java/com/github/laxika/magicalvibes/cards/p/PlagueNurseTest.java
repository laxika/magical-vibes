package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BiliousSkulldweller;
import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueNurse.class, BiliousSkulldweller.class, CrawlingChorus.class, ContagiousVorrac.class})
class PlagueNurseTest extends BaseCardTest {

    @Test
    @DisplayName("Grants toxic 1 to other toxic creatures you control")
    void grantsToxicToOtherToxicCreatures() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent toxicCreature = addReady(new BiliousSkulldweller());
        Permanent nonToxicCreature = addReady(new ContagiousVorrac());
        activate(nurse);

        assertThat(toxicCreature.getGrantedKeywords()).contains(Keyword.TOXIC);
        assertThat(nonToxicCreature.getGrantedKeywords()).doesNotContain(Keyword.TOXIC);
    }

    @Test
    @DisplayName("Granted toxic gives a poison counter when the creature deals combat damage")
    void grantedToxicGivesPoisonCounter() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent toxicCreature = addReady(new CrawlingChorus());
        activate(nurse);

        dealCombatDamage(toxicCreature);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Granted toxic wears off at end of turn")
    void toxicWearsOffAtEndOfTurn() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent toxicCreature = addReady(new BiliousSkulldweller());
        activate(nurse);

        assertThat(toxicCreature.getGrantedKeywords()).contains(Keyword.TOXIC);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(toxicCreature.getGrantedKeywords()).doesNotContain(Keyword.TOXIC);
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void canBeActivatedOnlyOnceEachTurn() {
        Permanent nurse = addReady(new PlagueNurse());
        addReady(new BiliousSkulldweller());
        activate(nurse);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Plague Nurse gives two poison counters with its own combat damage")
    void nurseHasToxicTwo() {
        Permanent nurse = addReady(new PlagueNurse());

        dealCombatDamage(nurse);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Additional toxic applies with combat damage without using the stack")
    void additionalToxicIsNotTriggered() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent creature = addReady(new BiliousSkulldweller());
        activate(nurse);

        dealCombatDamage(creature);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating does not increase the source's toxic value")
    void excludesSource() {
        Permanent nurse = addReady(new PlagueNurse());
        activate(nurse);

        dealCombatDamage(nurse);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain additional toxic")
    void excludesLaterCreatures() {
        Permanent nurse = addReady(new PlagueNurse());
        activate(nurse);
        Permanent creature = addReady(new BiliousSkulldweller());

        dealCombatDamage(creature);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Nurses each add one to another creature's toxic value")
    void separateNursesStack() {
        Permanent first = addReady(new PlagueNurse());
        Permanent second = addReady(new PlagueNurse());
        Permanent creature = addReady(new BiliousSkulldweller());
        activate(first);
        activate(second);

        dealCombatDamage(creature);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opposing toxic creatures do not receive the additional toxic")
    void excludesOpponentCreatures() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BiliousSkulldweller());
        activate(nurse);
        opponent.setSummoningSick(false);
        opponent.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the creature's original toxic remains after the turn ends")
    void poisonBonusExpires() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent creature = addReady(new BiliousSkulldweller());
        activate(nurse);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        dealCombatDamage(creature);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    private void dealCombatDamage(Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
    }

    private Permanent addReady(Card card) {
        return harness.addToBattlefieldAndReturn(player1, card);
    }

    private void activate(Permanent nurse) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(nurse);
        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();
    }
}
