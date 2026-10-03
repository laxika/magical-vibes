package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abjure;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.cards.f.Firestorm;
import com.github.laxika.magicalvibes.cards.f.FitOfRage;
import com.github.laxika.magicalvibes.cards.s.SpellCrumple;
import com.github.laxika.magicalvibes.cards.s.StripedBears;
import com.github.laxika.magicalvibes.cards.t.TolarianDrake;
import com.github.laxika.magicalvibes.cards.v.Vitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BosiumStrip.class, Abjure.class, BeanstalkGiant.class, FertileFootsteps.class,
        Firestorm.class, FitOfRage.class, SpellCrumple.class, StripedBears.class,
        TolarianDrake.class, Vitalize.class})
class BosiumStripTest extends BaseCardTest {

    private void addReadyStrip() {
        harness.addToBattlefield(player1, new BosiumStrip());
    }

    @Test
    @DisplayName("After activation, top instant of graveyard can be cast and is exiled")
    void castsTopInstantAndExiles() {
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        creature.tap();
        Vitalize vitalize = new Vitalize();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(creature.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Vitalize");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vitalize"));
    }

    @Test
    @DisplayName("Only the top graveyard card is castable; buried instant is not")
    void onlyTopCardIsCastable() {
        Vitalize buried = new Vitalize();
        Vitalize top = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(buried, top));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        harness.castAndResolveFlashback(player1, 1, null);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(buried);
    }

    @Test
    @DisplayName("After casting the top spell, the new top instant may also be cast")
    void canCastNextTopAfterFirst() {
        Vitalize first = new Vitalize();
        Vitalize second = new Vitalize();
        addReadyStrip();
        // first is buried, second is top; cast second then first becomes top
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 1, null);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Vitalize", "Vitalize");
    }

    @Test
    @DisplayName("Top creature card cannot be cast via the permission")
    void topCreatureCannotBeCast() {
        StripedBears bears = new StripedBears();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 4);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Without activating, cannot cast from the top of the graveyard")
    void cannotCastWithoutActivation() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("A sorcery on top of the graveyard can also be cast for its normal cost")
    void castsTopSorcery() {
        FitOfRage fitOfRage = new FitOfRage();
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        harness.setGraveyard(player1, List.of(fitOfRage));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveFlashback(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Fit of Rage");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fit of Rage"));
    }

    @Test
    @DisplayName("Permission wears off at end of turn")
    void permissionEndsAtEndOfTurn() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).contains(player1.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).doesNotContain(player1.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    void activationRequiresThreeManaAndTapsTheStrip() {
        Permanent strip = harness.addToBattlefieldAndReturn(player1, new BosiumStrip());
        harness.setGraveyard(player1, List.of(new Vitalize()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(strip.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(strip.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionDoesNotExistBeforeAbilityResolves() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        harness.passBothPriorities();
        harness.castAndResolveFlashback(player1, 0, null);
        assertThat(gd.findExiledCard(vitalize.getId())).isNotNull();
    }

    @Test
    void topNonSpellCardBlocksBuriedInstant() {
        Vitalize vitalize = new Vitalize();
        StripedBears bears = new StripedBears();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(vitalize, bears);
    }

    @Test
    void permissionBelongsOnlyToAbilityController() {
        addReadyStrip();
        harness.setGraveyard(player2, List.of(new Vitalize()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    void spellStillRequiresItsManaCost() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(vitalize);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 0, null);
        assertThat(gd.findExiledCard(vitalize.getId())).isNotNull();
    }

    @Test
    void instantCanBeCastDuringOpponentsTurnButSorceryCannot() {
        Vitalize vitalize = new Vitalize();
        FitOfRage fitOfRage = new FitOfRage();
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        creature.tap();
        harness.setGraveyard(player1, List.of(fitOfRage, vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 1, null);
        assertThat(creature.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fitOfRage);
    }

    @Test
    void counteredSpellIsExiledInsteadOfReturningToGraveyard() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        creature.tap();
        Permanent bluePermanent = harness.addToBattlefieldAndReturn(player2, new TolarianDrake());
        harness.setGraveyard(player1, List.of(vitalize));
        harness.setHand(player2, List.of(new Abjure()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passPriority(player1);

        harness.castInstantWithSacrifice(player2, 0, vitalize.getId(), bluePermanent.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.findExiledCard(vitalize.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Vitalize");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permissionSurvivesStripLeavingBattlefield() {
        Vitalize vitalize = new Vitalize();
        Permanent strip = harness.addToBattlefieldAndReturn(player1, new BosiumStrip());
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(strip);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.findExiledCard(vitalize.getId())).isNotNull();
    }

    @Test
    void spellCrumplePutsStripCastSpellInLibraryInsteadOfExile() {
        Vitalize vitalize = new Vitalize();
        SpellCrumple spellCrumple = new SpellCrumple();
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        creature.tap();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(spellCrumple));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromGraveyard(player1, 0);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, vitalize.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vitalize);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spellCrumple);
        assertThat(gd.findExiledCard(vitalize.getId())).isNull();
        harness.assertNotInGraveyard(player1, "Vitalize");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureSorceryCanBeCastFromTopOfGraveyard() {
        BeanstalkGiant giant = new BeanstalkGiant();
        addReadyStrip();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(giant));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAdventureFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(giant.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(giant.getId())).isEqualTo(player1.getId());
    }

    @Test
    void firestormCanBeCastByPayingItsAdditionalDiscardCost() {
        Firestorm firestorm = new Firestorm();
        Vitalize discarded = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(firestorm));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        gs.playFlashbackSpell(gd, player1, 0, 1, null, List.of(player2.getId()),
                null, null, List.of(), null, null, List.of(), Map.of(), List.of(), List.of(), List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.findExiledCard(firestorm.getId())).isNotNull();
    }
}
