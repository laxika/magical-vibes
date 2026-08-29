package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpittingSlug.class, GrizzlyBears.class, HighGround.class})
class SpittingSlugTest extends BaseCardTest {

    @Test
    @DisplayName("Declining when blocked gives first strike to every blocker")
    void decliningWhenBlockedGivesFirstStrikeToEveryBlocker() {
        Permanent slug = addReadySlug(player1);
        slug.setAttacking(true);
        Permanent blocker1 = addReadyBear(player2);
        Permanent blocker2 = addReadyBear(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, blocker1, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, blocker2, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slug, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Paying when blocked gives Spitting Slug first strike instead")
    void payingWhenBlockedGivesSlugFirstStrike() {
        Permanent slug = addReadySlug(player1);
        slug.setAttacking(true);
        Permanent blocker = addReadyBear(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, slug, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Declining when blocking gives first strike to every creature blocked by the Slug")
    void decliningWhenBlockingGivesFirstStrikeToEveryBlockedCreature() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent slug = addReadySlug(player2);
        Permanent attacker1 = addReadyBear(player1);
        Permanent attacker2 = addReadyBear(player1);
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.hasKeyword(gd, attacker1, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker2, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slug, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadySlug(Player player) {
        return addCreatureReady(player, new SpittingSlug());
    }

    private Permanent addReadyBear(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
