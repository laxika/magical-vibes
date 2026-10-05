package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImprovisationCapstone.class, Forest.class, GiantGrowth.class, GrizzlyBears.class,
        Shock.class, Twincast.class, RuleOfLaw.class})
class ImprovisationCapstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles from library until total mana value 4 or greater")
    void exilesUntilTotalManaValueFour() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName()).toList())
                .contains("Forest", "Forest", "Shock");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }

    @Test
    @DisplayName("Choosing Shock casts it without paying mana cost")
    void castsShockWithoutPaying() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearId);

        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
    }

    @Test
    @DisplayName("Remaining chosen spells are still cast after a targeted spell resolves its target")
    void castsAllChosenSpellsWhenNonLastSpellNeedsTarget() {
        Shock shock = new Shock();
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears1 =
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears2 =
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        // Shock (targeted) is exiled first, so it is cast before the two untargeted creature spells.
        harness.setLibrary(player1, List.of(shock, bears1, bears2));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), bears1.getId(), bears2.getId()));
        // Shock pauses for a target; resolving it must resume the queue for the remaining creatures.
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Grizzly Bears")).count())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Casting only untargeted spells leaves no parked resolution behind")
    void untargetedSpellsOnlyClearTheParkedResolution() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        // No spell in the queue asks for a target, so the queue drains without any further
        // interaction — the flow itself must resume the Capstone resolution parked for the choice.
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Grizzly Bears")).count())
                .isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry)
                .withFailMessage("dangling pendingEffectResolutionEntry after the cast queue drained")
                .isNull();
    }

    @Test
    @DisplayName("Casting no spells at all leaves no parked resolution behind")
    void decliningEveryCastClearsTheParkedResolution() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry)
                .withFailMessage("dangling pendingEffectResolutionEntry after declining every cast")
                .isNull();
    }

    @Test
    @DisplayName("A chosen spell with no legal target stays exiled")
    void chosenSpellWithoutLegalTargetsStaysExiled() {
        GiantGrowth growth1 = new GiantGrowth();
        GiantGrowth growth2 = new GiantGrowth();
        // No creature is on the battlefield, so neither Giant Growth can be legally cast.
        harness.setLibrary(player1, List.of(growth1, growth2, new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of(growth1.getId(), growth2.getId()));

        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(growth1.getId(), growth2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(c -> c.getName()))
                .doesNotContain("Giant Growth");
        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Giant Growth"))).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Stops exiling as soon as the cumulative mana value reaches four")
    void stopsAtThresholdAndLeavesRemainingLibraryUntouched() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock remaining = new Shock();
        harness.setLibrary(player1, List.of(new Forest(), first, second, remaining));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(first.getId(), second.getId()).doesNotContain(remaining.getId());
        harness.assertNotInGraveyard(player1, "Improvisation Capstone");
    }

    @Test
    @DisplayName("Resolving with only lands still exiles Capstone and enables future paradigm copies")
    void onlyLandsStillEstablishParadigm() {
        ImprovisationCapstone capstone = new ImprovisationCapstone();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(capstone));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(capstone.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Improvisation Capstone");
    }

    @Test
    @DisplayName("The first resolved Capstone copy establishes paradigm for its controller")
    void opponentWhoResolvesCopyGetsTheirOwnParadigmTrigger() {
        ImprovisationCapstone capstone = new ImprovisationCapstone();
        harness.setHand(player1, List.of(capstone));
        harness.setHand(player2, List.of(new Twincast()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, capstone.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
            assertThat(entry.getDescription()).isEqualTo("Improvisation Capstone paradigm");
        });
    }

    @Test
    @DisplayName("May cast a subset of the exiled spells and leave the others in exile")
    void castsOnlyChosenSpellAndLeavesOtherCardsExiled() {
        GrizzlyBears chosen = new GrizzlyBears();
        GrizzlyBears declined = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, chosen, declined));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(land.getId(), declined.getId()).doesNotContain(chosen.getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paradigm cannot cast a second spell under Rule of Law")
    void paradigmRespectsRuleOfLawAfterAnotherSpellWasCast() {
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneSatisfy(entry ->
                assertThat(entry.getCard().getName()).isEqualTo("Improvisation Capstone"));
    }
}
