package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bulette;
import com.github.laxika.magicalvibes.cards.c.CleverConjurer;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoloGuideToMonsters.class, GrizzlyBears.class, LlanowarElves.class,
        Bulette.class, CleverConjurer.class, HillGiantHerdgorger.class, SpareDagger.class})
class VoloGuideToMonstersTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature spell with no shared creature type as a token")
    void copiesCreatureSpellWithNoSharedType() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not copy a creature spell sharing a type with a graveyard creature card")
    void doesNotCopyCreatureSpellSharingGraveyardType() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.castFromHand(player1, new LlanowarElves(), "{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void volosOwnWizardTypePreventsCopying() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());

        harness.castFromHand(player1, new CleverConjurer(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Clever Conjurer");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void ignoresMatchingTypesInOpponentsZones() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.addToBattlefield(player2, new Bulette());
        harness.setGraveyard(player2, List.of(new Bulette()));

        harness.castFromHand(player1, new Bulette(), "{3}{G}");
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Bulette"))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken())
                .anyMatch(permanent -> !permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsCreatureSpell() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new Bulette(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Bulette");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotCopyNoncreatureSpell() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());

        harness.castFromHand(player1, new SpareDagger(), "{1}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Spare Dagger");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotRecheckCreatureTypesWhenTriggerResolves() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.castFromHand(player1, new Bulette(), "{3}{G}");
        assertThat(gd.stack).hasSize(2);

        harness.setGraveyard(player1, List.of(new Bulette()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Bulette"))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void originalAndTokenBothTriggerEnterAbilities() {
        harness.addToBattlefield(player1, new VoloGuideToMonsters());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 26);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Hill Giant Herdgorger"))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken())
                .anyMatch(permanent -> !permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }
}
