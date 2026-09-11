package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrapWelder.class, FellwarStone.class, Ornithopter.class})
class ScrapWelderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and returns a cheaper artifact with haste")
    void sacrificesArtifactAndReturnsCheaperArtifactWithHaste() {
        addReadyCreature(new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ornithopter");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Rejects an artifact whose mana value equals the sacrificed artifact")
    void rejectsArtifactWithEqualManaValue() {
        addReadyCreature(new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new FellwarStone();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Fellwar Stone");
    }

    @Test
    @DisplayName("Returned artifact loses the temporary haste at end of turn")
    void returnedArtifactLosesHasteAtEndOfTurn() {
        addReadyCreature(new ScrapWelder());
        harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ornithopter");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    private Permanent addReadyCreature(Card card) {
        return addCreatureReady(player1, card);
    }
}
