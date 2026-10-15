package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DruidLyrist;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MysticPenitent;
import com.github.laxika.magicalvibes.cards.s.ShiftingSky;
import com.github.laxika.magicalvibes.cards.s.StillLife;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantSuccession.class, Firebolt.class, DruidLyrist.class, MysticPenitent.class,
        Forest.class, ShiftingSky.class, StillLife.class})
class VerdantSuccessionTest extends BaseCardTest {

    @Test
    @DisplayName("A green nontoken creature's controller may search for a same-named card")
    void dyingCreatureControllerMaySearchForSameName() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new DruidLyrist(), new Forest()));
        prepareRemoval(lyrist);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement().extracting(Card::getName).isEqualTo("Druid Lyrist");

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).hasSize(1);
        assertThat(findPermanents(player2, "Druid Lyrist").getFirst().isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> "Druid Lyrist".equals(card.getName()));
    }

    @Test
    @DisplayName("The dying creature's controller may decline the search")
    void searchMayBeDeclined() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new DruidLyrist()));
        prepareRemoval(lyrist);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).isEmpty();
    }

    @Test
    @DisplayName("The search completes without a card when no same-named card is in the library")
    void searchWithNoMatchingCardCompletes() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new Forest()));
        prepareRemoval(lyrist);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).isEmpty();
    }

    @Test
    @DisplayName("Only green nontoken creature deaths trigger the ability")
    void nonGreenAndTokenDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent penitent = harness.addToBattlefieldAndReturn(player2, new MysticPenitent());
        prepareRemoval(penitent);
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());
        prepareRemoval(token);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search may fail to find even with a matching card available")
    void mayFailToFindMatchingCard() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new DruidLyrist(), new Forest()));
        prepareRemoval(lyrist);

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2)
                .anyMatch(card -> "Druid Lyrist".equals(card.getName()));
    }

    @Test
    @DisplayName("The enchantment's controller controls the trigger for an opponent's creature")
    void enchantmentControllerControlsTrigger() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        dealFireboltDamage(lyrist);

        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
    }

    @Test
    @DisplayName("A creature made green on the battlefield triggers the search")
    void creatureMadeGreenTriggers() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent sky = harness.addToBattlefieldAndReturn(player1, new ShiftingSky());
        sky.setChosenColor(CardColor.GREEN);
        Permanent penitent = harness.addToBattlefieldAndReturn(player2, new MysticPenitent());
        harness.setLibrary(player2, List.of(new MysticPenitent()));
        prepareRemoval(penitent);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanents(player2, "Mystic Penitent")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A printed green creature made white on the battlefield does not trigger")
    void creatureMadeNonGreenDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent sky = harness.addToBattlefieldAndReturn(player1, new ShiftingSky());
        sky.setChosenColor(CardColor.WHITE);
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        prepareRemoval(lyrist);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated enchantment's death can find an unanimated card of the same name")
    void animatedEnchantmentFindsSameName() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent stillLife = harness.addToBattlefieldAndReturn(player1, new StillLife());
        harness.setLibrary(player1, List.of(new StillLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        dealFireboltDamage(stillLife);
        prepareRemoval(stillLife);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Still Life")).hasSize(1);
        assertThat(gqs.isCreature(gd, findPermanents(player1, "Still Life").getFirst())).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void prepareRemoval(Permanent target) {
        dealFireboltDamage(target);
        harness.passBothPriorities();
    }

    private void dealFireboltDamage(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Saproling Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
