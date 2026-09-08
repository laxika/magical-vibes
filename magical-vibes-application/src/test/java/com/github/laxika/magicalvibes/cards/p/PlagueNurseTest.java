package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BiliousSkulldweller;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlagueNurseTest extends BaseCardTest {

    @Test
    @DisplayName("Grants toxic 1 to other toxic creatures you control")
    void grantsToxicToOtherToxicCreatures() {
        Permanent nurse = addReady(new PlagueNurse());
        Permanent toxicCreature = addReady(new BiliousSkulldweller());
        Permanent nonToxicCreature = addReady(new GrizzlyBears());
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

        toxicCreature.setSummoningSick(false);
        toxicCreature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
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

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
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
