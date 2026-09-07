package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoloGuideToMonsters.class, GrizzlyBears.class, LlanowarElves.class})
class VoloGuideToMonstersTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature spell with no shared creature type as a token")
    void copiesCreatureSpellWithNoSharedType() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(entry.isCopy()).isTrue();
        });

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent ->
                assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Does not copy a creature spell sharing a type with a controlled creature")
    void doesNotCopyCreatureSpellSharingControlledType() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not copy a creature spell sharing a type with a graveyard creature card")
    void doesNotCopyCreatureSpellSharingGraveyardType() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
