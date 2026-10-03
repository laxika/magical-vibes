package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderfangVenom.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class ElderfangVenomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Elves you control have deathtouch")
    void attackingElvesHaveDeathtouch() {
        harness.addToBattlefield(player1, new ElderfangVenom());
        Permanent attackingElf = addCreatureReady(player1, new LlanowarElves());
        Permanent nonAttackingElf = addCreatureReady(player1, new LlanowarElves());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());

        assertThat(gqs.hasKeyword(gd, attackingElf, Keyword.DEATHTOUCH)).isFalse();

        declareAttackers(player1, List.of(1));

        assertThat(gqs.hasKeyword(gd, attackingElf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttackingElf, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An Elf you control dying drains each opponent and gains you life")
    void allyElfDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new ElderfangVenom());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, elf);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A non-Elf or an opponent's Elf dying does not trigger the drain")
    void unrelatedCreatureDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new ElderfangVenom());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, nonElf);
        killWithShock(player1, opponentElf);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
