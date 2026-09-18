package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HookSwords.class, GrizzlyBears.class})
class HookSwordsTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsAnAlly() {
        harness.setHand(player1, List.of(new HookSwords()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent swords = findPermanent(player1, "Hook Swords");
        Permanent ally = findPermanent(player1, "Ally");

        harness.forceActivePlayer(player1);
        assertThat(swords.getAttachedTo()).isEqualTo(ally.getId());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void bonusOnlyAppliesDuringTheControllersTurn() {
        Permanent swords = addSwordsReady(player1);
        Permanent creature = addCreatureReady(player1);
        swords.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void equipMovesTheConditionalBonus() {
        Permanent swords = addSwordsReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        swords.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(swords.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent addSwordsReady(Player player) {
        Permanent permanent = new Permanent(new HookSwords());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
