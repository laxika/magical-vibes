package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JorubaiMurkLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/3 without a Swamp")
    void noBoostWithoutSwamp() {
        harness.addToBattlefield(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player1, new Forest());

        Permanent lurker = findPermanent(player1, "Jorubai Murk Lurker");
        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 while its controller controls a Swamp")
    void boostWithSwamp() {
        harness.addToBattlefield(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player1, new Swamp());

        Permanent lurker = findPermanent(player1, "Jorubai Murk Lurker");
        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's Swamp does not grant the boost")
    void noBoostFromOpponentSwamp() {
        harness.addToBattlefield(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player2, new Swamp());

        Permanent lurker = findPermanent(player1, "Jorubai Murk Lurker");
        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(3);
    }

    @Test
    @DisplayName("{1}{B} grants target creature lifelink until end of turn")
    void activatedAbilityGrantsLifelinkToTargetCreature() {
        addLurkerReady(player1);
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        addLurkerReady(player1);
        Permanent target = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addLurkerReady(Player player) {
        Permanent lurker = new Permanent(new JorubaiMurkLurker());
        lurker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lurker);
        return lurker;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
