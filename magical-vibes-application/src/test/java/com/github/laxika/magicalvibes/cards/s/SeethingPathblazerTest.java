package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeethingPathblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Elemental gives +2/+0 and first strike until end of turn")
    void sacrificeElementalBoostsAndGrantsFirstStrike() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        addCreatureReady(player1, new AirElemental());
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elementalId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(4); // 2 base + 2
        assertThat(gqs.getEffectiveToughness(gd, pathblazer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isTrue();

        // The Air Elemental was sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        addCreatureReady(player1, new AirElemental());
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elementalId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can sacrifice itself to its own ability (only Elemental available)")
    void canSacrificeItself() {
        addCreatureReady(player1, new SeethingPathblazer());

        harness.activateAbility(player1, 0, null, null);

        // Pathblazer sacrificed itself; the ability is on the stack but will fizzle
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Seething Pathblazer"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Seething Pathblazer"));
    }
}
