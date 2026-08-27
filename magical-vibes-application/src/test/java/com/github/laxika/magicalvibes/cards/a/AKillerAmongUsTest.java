package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AKillerAmongUs.class)
class AKillerAmongUsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with Human, Merfolk, and Goblin tokens and keeps the creature type secret")
    void createsTokensAndHidesChoice() {
        castAndChoose(CardSubtype.HUMAN);

        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        assertThat(gameLogContains("chooses Human for A Killer Among Us")).isFalse();
    }

    @Test
    @DisplayName("A matching attacking token gets three counters and deathtouch after the source is sacrificed")
    void matchingAttackingTokenGetsCountersAndDeathtouch() {
        Permanent killer = castAndChoose(CardSubtype.HUMAN);
        Permanent human = findPermanent(player1, "Human");
        human.setSummoningSick(false);
        human.setAttacking(true);

        harness.activateAbility(player1, battlefieldIndex(killer), 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, human, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gameLogContains("reveals the chosen creature type: Human")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(killer);
    }

    @Test
    @DisplayName("A different attacking token can be targeted but gets no bonus")
    void differentAttackingTokenGetsNoBonus() {
        Permanent killer = castAndChoose(CardSubtype.HUMAN);
        Permanent merfolk = findPermanent(player1, "Merfolk");
        merfolk.setSummoningSick(false);
        merfolk.setAttacking(true);

        harness.activateAbility(player1, battlefieldIndex(killer), 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent castAndChoose(CardSubtype subtype) {
        harness.setHand(player1, List.of(new AKillerAmongUs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
        harness.passBothPriorities();
        return findPermanent(player1, "A Killer Among Us");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
