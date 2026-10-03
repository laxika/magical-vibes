package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BootsOfSpeed;
import com.github.laxika.magicalvibes.cards.c.CeremonialKnife;
import com.github.laxika.magicalvibes.cards.c.CliffhavenKitesail;
import com.github.laxika.magicalvibes.cards.c.ColossusHammer;
import com.github.laxika.magicalvibes.cards.d.DuelingRapier;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.m.MaskOfImmolation;
import com.github.laxika.magicalvibes.cards.m.MirrorShield;
import com.github.laxika.magicalvibes.cards.r.RelicAxe;
import com.github.laxika.magicalvibes.cards.r.RoguesGloves;
import com.github.laxika.magicalvibes.cards.s.ScavengedBlade;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheRealm;
import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.cards.t.TormentorsHelm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmsScavenger.class, BootsOfSpeed.class, CliffhavenKitesail.class,
        ColossusHammer.class, DuelingRapier.class, SpareDagger.class, TormentorsHelm.class,
        GoldveinPick.class, JoustingLance.class, MaskOfImmolation.class, MirrorShield.class,
        RelicAxe.class, RoguesGloves.class, ScavengedBlade.class, ShieldOfTheRealm.class,
        CeremonialKnife.class, GrizzlyBears.class, LeoninScimitar.class})
class ArmsScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, Arms Scavenger drafts an Equipment into exile with permission to play it")
    void draftsAndExilesSpellbookCard() {
        addCreatureReady(player1, new ArmsScavenger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(drafted);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drafted);
        assertThat(gd.exilePlayPermissions).containsEntry(drafted.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(drafted.getId());
    }

    @Test
    @DisplayName("The drafted card can be played from exile during the turn")
    void playsDraftedCardFromExile() {
        addCreatureReady(player1, new ArmsScavenger());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        if (TARGETED_ON_ENTRY.contains(drafted.getName())) {
            harness.castFromExile(player1, drafted.getId(), target.getId());
        } else {
            harness.castFromExile(player1, drafted.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(drafted.getId())).isNull();
    }

    @Test
    @DisplayName("The drafted card's play permission expires at end of turn")
    void draftedCardPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ArmsScavenger());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(drafted.getId());
        assertThat(gd.findExiledCard(drafted.getId())).isNotNull();
    }

    @Test
    @DisplayName("Equip abilities you activate cost one less")
    void reducesEquipCost() {
        addCreatureReady(player1, new ArmsScavenger());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar),
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Multiple Arms Scavengers reduce generic equip costs but not colored mana")
    void stackedReductionsPreserveColoredEquipCost() {
        Permanent creature = addCreatureReady(player1, new ArmsScavenger());
        addCreatureReady(player1, new ArmsScavenger());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ScavengedBlade());
        int bladeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blade);

        assertThatThrownBy(() -> harness.activateAbility(player1, bladeIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, bladeIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Arms Scavenger does not reduce an opponent's equip costs")
    void doesNotReduceOpponentsEquipCost() {
        addCreatureReady(player1, new ArmsScavenger());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int scimitarIndex = gd.playerBattlefields.get(player2.getId()).indexOf(scimitar);

        assertThatThrownBy(() -> harness.activateAbility(player2, scimitarIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, scimitarIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Arms Scavenger does not draft during an opponent's upkeep")
    void doesNotDraftDuringOpponentsUpkeep() {
        addCreatureReady(player1, new ArmsScavenger());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A draft offers three distinct spellbook cards and exiles only the selection")
    void offersThreeDistinctCardsAndExilesOnlyChosenCard() {
        addCreatureReady(player1, new ArmsScavenger());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards().stream().map(Card::getName).toList())
                .doesNotHaveDuplicates()
                .isSubsetOf("Boots of Speed", "Cliffhaven Kitesail", "Colossus Hammer",
                        "Dueling Rapier", "Spare Dagger", "Tormentor's Helm", "Goldvein Pick",
                        "Jousting Lance", "Mask of Immolation", "Mirror Shield", "Relic Axe",
                        "Rogue's Gloves", "Scavenged Blade", "Shield of the Realm", "Ceremonial Knife");
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(drafted);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(choice.cards());
        for (Card unchosen : choice.cards().subList(1, 3)) {
            assertThat(gd.exilePlayPermissions).doesNotContainKey(unchosen.getId());
        }
    }

    private static final Set<String> TARGETED_ON_ENTRY = Set.of(
            "Cliffhaven Kitesail", "Dueling Rapier", "Relic Axe", "Scavenged Blade");
}
