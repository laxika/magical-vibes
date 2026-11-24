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
        assertThat(gd.exiledCardsWithCounter(CounterType.DREAM)).containsEntry(ritual.getId(), 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ritual.getId()));
    }

    @Test
    @DisplayName("Attacking offers one owned dream-counter spell to cast for free")
    void attackingOffersDreamCounterSpell() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.setExiledCardCounters(ritual.getId(), CounterType.DREAM, 1);

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
    @DisplayName("Declining the free cast leaves the dream-counter card in exile")
    void decliningLeavesTheDreamCardInExile() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.setExiledCardCounters(ritual.getId(), CounterType.DREAM, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardsWithCounter(CounterType.DREAM)).containsEntry(ritual.getId(), 1);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
    }

    @Test
    @DisplayName("Attacking can't cast a dream-counter card an opponent owns")
    void attackingCannotCastAnOpponentsDreamCard() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player2, List.of(ritual));
        gd.setExiledCardCounters(ritual.getId(), CounterType.DREAM, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
    }

    @Test
    @DisplayName("One attack chooses and casts only one of several dream-counter spells, which isn't exiled again")
    void oneAttackCastsOnlyOneDreamSpellAndDoesNotExileItAgain() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        harness.setExile(player1, List.of(first, second));
        gd.setExiledCardCounters(first.getId(), CounterType.DREAM, 1);
        gd.setExiledCardCounters(second.getId(), CounterType.DREAM, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).containsExactlyInAnyOrder(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof DarkRitual).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(card -> card instanceof DarkRitual).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({VirulentSwipe.class})
    @DisplayName("A resolving rebound spell lets its controller choose which replacement applies")
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
    @DisplayName("A free cast still requires the spell's additional blight or mana cost")
    void freeCastStillRequiresTheAdditionalBlightOrManaCost() {
        Permanent daydreamer = addCreatureReady(player1, new GoliathDaydreamer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathDaydreamer());
        BogslithersEmbrace spell = new BogslithersEmbrace();
        harness.setExile(player1, List.of(spell));
        gd.setExiledCardCounters(spell.getId(), CounterType.DREAM, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        boolean castCompleted = gd.stack.stream().anyMatch(entry -> entry.getCard().getId().equals(spell.getId()))
                || gd.findExiledCard(target.getCard().getId()) != null;
        assertThat(castCompleted && daydreamer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) == 0).isFalse();
    }

    @Test
    @DisplayName("Does not exile a hand-cast spell owned by another player, which goes to its owner's graveyard")
    void doesNotExileSpellOwnedByAnotherPlayer() {
        harness.addToBattlefield(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        ritual.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(ritual.getId())).isNull();
        assertThat(gd.exiledCardsWithCounter(CounterType.DREAM)).doesNotContainKey(ritual.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(ritual.getId()));
    }
}
