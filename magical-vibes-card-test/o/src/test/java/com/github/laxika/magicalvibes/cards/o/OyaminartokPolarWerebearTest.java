package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Archipelagore;
import com.github.laxika.magicalvibes.cards.j.JunkWinder;
import com.github.laxika.magicalvibes.cards.m.MoatPiranhas;
import com.github.laxika.magicalvibes.cards.m.MysticSkyfish;
import com.github.laxika.magicalvibes.cards.n.NadirKraken;
import com.github.laxika.magicalvibes.cards.n.NezahalPrimalTide;
import com.github.laxika.magicalvibes.cards.p.PouncingShoreshark;
import com.github.laxika.magicalvibes.cards.p.PursuedWhale;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.cards.r.RuinCrab;
import com.github.laxika.magicalvibes.cards.s.SeaDasherOctopus;
import com.github.laxika.magicalvibes.cards.s.SigiledStarfish;
import com.github.laxika.magicalvibes.cards.s.SpinedMegalodon;
import com.github.laxika.magicalvibes.cards.s.StingingLionfish;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.v.VoraciousGreatshark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OyaminartokPolarWerebear.class, Archipelagore.class, JunkWinder.class,
        MoatPiranhas.class, MysticSkyfish.class, NadirKraken.class, NezahalPrimalTide.class,
        PouncingShoreshark.class, PursuedWhale.class, RiptideTurtle.class, RuinCrab.class,
        SeaDasherOctopus.class, SigiledStarfish.class, SpinedMegalodon.class,
        StingingLionfish.class, VoraciousGreatshark.class, Food.class})
class OyaminartokPolarWerebearTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof until it deals damage")
    void hasHexproofUntilDealingDamage() {
        Permanent werebear = addCreatureReady(player1, new OyaminartokPolarWerebear());

        assertThat(gqs.hasKeyword(gd, werebear, Keyword.HEXPROOF)).isTrue();

        werebear.setAttacking(true);
        resolveCombat();

        assertThat(gqs.hasKeyword(gd, werebear, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Creates a Food token when it deals combat damage to a player")
    void createsFoodTokenOnCombatDamageToPlayer() {
        Permanent werebear = addCreatureReady(player1, new OyaminartokPolarWerebear());
        werebear.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Sacrifices a Food to draft and then adds restricted blue mana")
    void sacrificesFoodToDraftAndAddRestrictedMana() {
        harness.addToBattlefield(player1, new OyaminartokPolarWerebear());
        harness.addToBattlefield(player1, new Food());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new MysticSkyfish()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mystic Skyfish");
    }

    @Test
    @DisplayName("Drafting creates a separate mana trigger that players can respond to")
    void draftingCreatesRespondableManaTrigger() {
        beginDraft();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards().stream().map(Card::getName).toList()).doesNotHaveDuplicates();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Draft mana cannot pay the generic cost of a nonblue creature")
    void draftManaCannotCastNonblueCreature() {
        finishDraft();
        harness.setHand(player1, List.of(new OyaminartokPolarWerebear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Food cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsFood() {
        harness.addToBattlefield(player1, new OyaminartokPolarWerebear());
        harness.addToBattlefield(player2, new Food());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Food");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to a blocker removes hexproof without creating Food")
    void damageToBlockerRemovesHexproofWithoutFood() {
        Permanent werebear = addCreatureReady(player1, new OyaminartokPolarWerebear());
        werebear.setAttacking(true);
        harness.addToBattlefield(player2, new RiptideTurtle());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, werebear, Keyword.HEXPROOF)).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Riptide Turtle");
    }

    @Test
    @DisplayName("Hexproof does not return on a later turn after dealing damage")
    void hexproofDoesNotReturnOnLaterTurn() {
        Permanent werebear = addCreatureReady(player1, new OyaminartokPolarWerebear());
        werebear.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        advanceToUpkeep(player2);

        assertThat(gqs.hasKeyword(gd, werebear, Keyword.HEXPROOF)).isFalse();
    }

    private void beginDraft() {
        harness.addToBattlefield(player1, new OyaminartokPolarWerebear());
        harness.addToBattlefield(player1, new Food());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNotNull();
    }

    private void finishDraft() {
        beginDraft();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        resolveAllTriggers();
    }
}
