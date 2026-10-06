package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestorationSeminar.class, GrizzlyBears.class, Forest.class, Shock.class,
        HolyStrength.class, Spellbook.class, Twincast.class, RuleOfLaw.class})
class RestorationSeminarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns targeted nonland permanent from graveyard to battlefield")
    void returnsNonlandPermanentToBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot target land card in graveyard")
    void cannotTargetLandInGraveyard() {
        var forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent (instant/sorcery) card in graveyard")
    void cannotTargetInstantInGraveyard() {
        var shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paradigm exiles the spell and registers delayed trigger on first resolve")
    void paradigmExilesAndRegisters() {
        RestorationSeminar seminar = new RestorationSeminar();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(seminar));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Restoration Seminar");
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Restoration Seminar"))).isTrue();
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
        assertThat(gd.paradigmResolvedSpellNames.get(player1.getId())).contains("Restoration Seminar");
    }

    @Test
    void returnsNoncreatureArtifact() {
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        castSeminar(artifact.getId());

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void returnsNonAuraEnchantment() {
        RuleOfLaw enchantment = new RuleOfLaw();
        harness.setGraveyard(player1, List.of(enchantment));
        castSeminar(enchantment.getId());

        harness.assertOnBattlefield(player1, "Rule of Law");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(enchantment);
    }

    @Test
    void cannotTargetSorcery() {
        RestorationSeminar otherSeminar = new RestorationSeminar();
        harness.setGraveyard(player1, List.of(otherSeminar));
        prepareSeminar();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, otherSeminar.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        prepareSeminar();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingTargetPreventsResolutionAndParadigm() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        prepareSeminar();
        harness.castSorcery(player1, 0, bears.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Restoration Seminar");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Restoration Seminar"));
        assertThat(gd.paradigmDelayedTriggers).isEmpty();
    }

    @Test
    void returnedAuraChoosesOpponentsCreatureToEnchant() {
        HolyStrength aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID hostId = harness.getPermanentId(player2, "Grizzly Bears");

        castSeminar(aura.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, hostId);

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
            assertThat(permanent.getAttachedTo()).isEqualTo(hostId);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        harness.assertNotInGraveyard(player1, "Restoration Seminar");
    }

    @Test
    void paradigmCopyReturnsNewTargetWithoutManaAndCanBeDeclinedEarlier() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        castSeminar(first.getId());

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(second.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(second);
        harness.assertNotInGraveyard(player1, "Restoration Seminar");
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Restoration Seminar"))).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void paradigmCopyWithNoLegalTargetCeasesToExist() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castSeminar(bears.getId());

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Restoration Seminar");
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Restoration Seminar"))).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void firstResolvedSpellCopyEstablishesParadigmEvenWhenOriginalCannotResolve() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        RestorationSeminar seminar = new RestorationSeminar();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(seminar, new Twincast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, first.getId());
        harness.castAndResolveInstant(player1, 0, seminar.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(first.getId()));
        harness.assertInGraveyard(player1, "Restoration Seminar");
        advanceToFirstMainPhase();
        assertThat(gd.stack).anyMatch(entry ->
                entry.getDescription().equals("Restoration Seminar paradigm"));
    }

    @Test
    void paradigmCannotCastSecondSpellUnderRuleOfLaw() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        castSeminar(first.getId());
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
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, second.getId());
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(entry ->
                entry.getCard().getName().equals("Restoration Seminar"));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }

    @Test
    void resolvingTwoOriginalsCreatesOnlyOneRecurringCopy() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third));
        castSeminar(first.getId());
        castSeminar(second.getId());

        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Restoration Seminar"))).hasSize(2);
        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(third.getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Restoration Seminar"))).hasSize(2);
    }

    private void prepareSeminar() {
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void castSeminar(UUID targetId) {
        prepareSeminar();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void advanceToFirstMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
