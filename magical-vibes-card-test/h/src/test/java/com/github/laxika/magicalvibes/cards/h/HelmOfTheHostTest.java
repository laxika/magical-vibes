package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.i.InvokeTheDivine;
import com.github.laxika.magicalvibes.cards.d.DivineVisitation;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelmOfTheHost.class, GrizzlyBears.class, HallarTheFirefletcher.class,
        Clone.class, InvokeTheDivine.class, BlinkOfAnEye.class, DivineVisitation.class})
class HelmOfTheHostTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping requires five mana")
    void equipRequiresFiveMana() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helm.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving equip ability attaches Helm to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("At beginning of combat, creates a token copy of equipped creature")
    void createsTokenCopyAtBeginningOfCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        // Advance from precombat main to beginning of combat
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities(); // resolve the triggered ability

        // Should have the original creature + a token copy
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy has haste")
    void tokenCopyHasHaste() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Token copy of legendary creature is not legendary")
    void tokenCopyNotLegendary() {
        Permanent legendary = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(legendary.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hallar, the Firefletcher") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Token copy of non-legendary creature preserves supertypes")
    void tokenCopyPreservesNonLegendarySupertypes() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        // Grizzly Bears has no supertypes, so the token shouldn't either
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Token copy has same power and toughness as equipped creature")
    void tokenCopySamePowerToughness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("No token created when Helm is not attached to any creature")
    void noTokenWhenNotAttached() {
        addCreatureReady(player1, new GrizzlyBears());
        addHelmReady(player1);
        // Helm is NOT attached

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Trigger only fires on controller's turn")
    void triggerOnlyOnControllersTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        // Opponent's turn — trigger should not fire
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Equipped creature leaving before resolution means no token")
    void equippedCreatureLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        // Remove the equipped creature before resolution
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        helm.setAttachedTo(null);

        harness.passBothPriorities(); // resolve

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Copying a Helm token preserves nonlegendary status but does not copy granted haste")
    void copyingTokenDoesNotCopyGrantedHaste() {
        Permanent creature = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard() instanceof Clone).findFirst().orElseThrow();
        assertThat(clone.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gqs.hasKeyword(gd, clone, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The trigger still copies the last equipped creature after Helm leaves")
    void createsTokenAfterHelmLeaves() {
        Permanent creature = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        harness.setHand(player2, java.util.List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, helm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> {
                    assertThat(p.getCard().isToken()).isTrue();
                    assertThat(p.getCard().getName()).isEqualTo(creature.getCard().getName());
                });
    }

    @Test
    @DisplayName("After Helm leaves, the trigger uses last known information if the equipped creature also leaves")
    void createsTokenAfterHelmAndCreatureLeave() {
        Permanent creature = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        harness.setHand(player2, java.util.List.of(new InvokeTheDivine(), new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, helm.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> {
                    assertThat(p.getCard().isToken()).isTrue();
                    assertThat(p.getCard().getName()).isEqualTo("Hallar, the Firefletcher");
                    assertThat(p.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
                    assertThat(gqs.hasKeyword(gd, p, Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("The creature equipped when the trigger resolves is copied")
    void copiesCreatureEquippedAtResolution() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(first.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        helm.setAttachedTo(second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(p ->
                        assertThat(p.getCard().getName()).isEqualTo(second.getCard().getName()));
    }

    @Test
    @DisplayName("Helm's controller receives the token even if the equipped creature is controlled by an opponent")
    void tokenBelongsToHelmController() {
        Permanent creature = addLegendaryCreature(player2);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Divine Visitation's replacement token still gains haste from Helm")
    void replacementTokenGainsHaste() {
        Permanent creature = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        harness.addToBattlefield(player1, new DivineVisitation());
        helm.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).singleElement().satisfies(p -> {
                    assertThat(p.getCard().getName()).isEqualTo("Angel");
                    assertThat(gqs.hasKeyword(gd, p, Keyword.FLYING)).isTrue();
                    assertThat(gqs.hasKeyword(gd, p, Keyword.VIGILANCE)).isTrue();
                    assertThat(gqs.hasKeyword(gd, p, Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addLegendaryCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addLegendaryCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("A token copy retains all of the equipped creature's colors")
    void copiesAllColorsOfMulticoloredCreature() {
        Permanent creature = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).singleElement().satisfies(p ->
                        assertThat(gqs.getEffectiveColors(gd, p))
                                .containsExactlyInAnyOrderElementsOf(gqs.getEffectiveColors(gd, creature)));
    }

    private Permanent addHelmReady(Player player) {
        return addCreatureReady(player, new HelmOfTheHost());
    }

    private Permanent addLegendaryCreature(Player player) {
        return addCreatureReady(player, new HallarTheFirefletcher());
    }
}
