package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.b.BogslithersEmbrace;
import com.github.laxika.magicalvibes.cards.v.VirulentSwipe;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoliathDaydreamer.class, DarkRitual.class})
class GoliathDaydreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a hand-cast instant with a dream counter after it resolves")
    void exilesResolvingHandSpellWithDreamCounter() {
        harness.addToBattlefield(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardDreamCounters).containsEntry(ritual.getId(), 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ritual.getId()));
    }

    @Test
    @DisplayName("Attacking offers one owned dream-counter spell to cast for free")
    void attackingOffersDreamCounterSpell() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.exiledCardDreamCounters.put(ritual.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(ritual.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
    }

    @Test
    @DisplayName("Attacking does not offer an exiled spell without a dream counter")
    void attackingIgnoresExiledSpellWithoutDreamCounter() {
        addCreatureReady(player1, new GoliathDaydreamer());
        harness.setExile(player1, List.of(new DarkRitual()));

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void decliningLeavesTheDreamCardInExile() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.exiledCardDreamCounters.put(ritual.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardDreamCounters).containsEntry(ritual.getId(), 1);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
    }

    @Test
    void attackingCannotCastAnOpponentsDreamCard() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player2, List.of(ritual));
        gd.exiledCardDreamCounters.put(ritual.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
    }

    @Test
    void oneAttackCastsOnlyOneDreamSpellAndDoesNotExileItAgain() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        harness.setExile(player1, List.of(first, second));
        gd.exiledCardDreamCounters.put(first.getId(), 1);
        gd.exiledCardDreamCounters.put(second.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof DarkRitual).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(card -> card instanceof DarkRitual).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({VirulentSwipe.class})
    void resolvingReboundSpellLetsControllerChooseItsReplacement() {
        Permanent daydreamer = harness.addToBattlefieldAndReturn(player1, new GoliathDaydreamer());
        VirulentSwipe swipe = new VirulentSwipe();
        harness.setHand(player1, List.of(swipe));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, daydreamer.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.findExiledCard(swipe.getId())).isNull();
    }

    @Test
    @CardUsed({BogslithersEmbrace.class})
    void freeCastStillRequiresTheAdditionalBlightOrManaCost() {
        Permanent daydreamer = addCreatureReady(player1, new GoliathDaydreamer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathDaydreamer());
        BogslithersEmbrace spell = new BogslithersEmbrace();
        harness.setExile(player1, List.of(spell));
        gd.exiledCardDreamCounters.put(spell.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        boolean castCompleted = gd.stack.stream().anyMatch(entry -> entry.getCard().getId().equals(spell.getId()))
                || gd.findExiledCard(target.getCard().getId()) != null;
        assertThat(castCompleted && daydreamer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) == 0).isFalse();
    }
}
