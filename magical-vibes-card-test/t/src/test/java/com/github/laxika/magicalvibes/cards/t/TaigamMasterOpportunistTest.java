package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DragonSniper;
import com.github.laxika.magicalvibes.cards.d.DustOfMoments;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaigamMasterOpportunist.class, DarkRitual.class, LightningBolt.class, DragonSniper.class, DustOfMoments.class})
class TaigamMasterOpportunistTest extends BaseCardTest {

    @Test
    void copiesTheSecondSpellAndSuspendsTheOriginal() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        LightningBolt first = new LightningBolt();
        LightningBolt second = new LightningBolt();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.suspendedSpellExiles)
                .anySatisfy(pending -> {
                    assertThat(pending.cardId()).isEqualTo(second.getId());
                    assertThat(pending.counters()).isEqualTo(4);
                });
        assertThat(gd.stack).filteredOn(com.github.laxika.magicalvibes.model.StackEntry::isCopy).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removesTimeCountersAndOffersTheSpellAfterTheLastOne() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        for (int expectedCounters = 3; expectedCounters > 0; expectedCounters--) {
            int counters = expectedCounters;
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            assertThat(gd.suspendedSpellExiles).anySatisfy(pending -> {
                assertThat(pending.cardId()).isEqualTo(second.getId());
                assertThat(pending.counters()).isEqualTo(counters);
            });
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(second.getId()));
    }

    @Test
    void firstAndThirdSpellsAreNotCopiedOrExiled() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        DarkRitual third = new DarkRitual();
        harness.setHand(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.suspendedSpellExiles).hasSize(1);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSecondSpellDoesNotTriggerTaigam() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        harness.setHand(player2, List.of(first, second));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureCopyBecomesTokenAndSuspendedOriginalReturnsWithHaste() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        harness.castFromHand(player1, new DragonSniper(), "{G}");
        harness.passBothPriorities();
        DragonSniper second = new DragonSniper();
        harness.castFromHand(player1, second, "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon Sniper")).hasSize(2)
                .anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(gd.findExiledCard(second.getId())).isNotNull();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.suspendedSpellExiles).anySatisfy(pending ->
                assertThat(pending.counters()).isEqualTo(4));

        for (int upkeep = 0; upkeep < 4; upkeep++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon Sniper")).hasSize(3)
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(second.getId());
                    assertThat(permanent.getCard().isToken()).isFalse();
                    assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
                });
    }

    @Test
    void decliningSuspendCastLeavesTheCardExiledWithoutAnotherOffer() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        harness.castFromHand(player1, new DragonSniper(), "{G}");
        harness.passBothPriorities();
        DragonSniper second = new DragonSniper();
        harness.castFromHand(player1, second, "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (int upkeep = 0; upkeep < 4; upkeep++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        advanceToUpkeep(player1);

        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void suspendedOriginalCanLoseTimeCountersToDustOfMoments() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        harness.castFromHand(player1, new DragonSniper(), "{G}");
        harness.passBothPriorities();
        DragonSniper second = new DragonSniper();
        harness.castFromHand(player1, second, "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new DustOfMoments()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.suspendedSpellExiles).anySatisfy(pending -> {
            assertThat(pending.cardId()).isEqualTo(second.getId());
            assertThat(pending.counters()).isEqualTo(2);
        });
    }
}
