package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanPlanarTrailblazer.class, Forest.class})
class KellanPlanarTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("The Scout ability becomes a Detective without changing base stats")
    void scoutAbilityBecomesDetective() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.DETECTIVE)
                .doesNotContain(CardSubtype.SCOUT);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Detective ability becomes a 3/2 Rogue with double strike")
    void detectiveAbilityBecomesRogueWithDoubleStrike() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.ROGUE)
                .doesNotContain(CardSubtype.SCOUT, CardSubtype.DETECTIVE);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kellan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The Detective combat-damage ability exiles the top card with play permission")
    void combatDamageExilesTopCardToPlay() {
        addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    void detectiveAbilityDoesNothingWhileScout() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan)).contains(CardSubtype.SCOUT)
                .doesNotContain(CardSubtype.ROGUE);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, kellan, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void stackedScoutActivationsCheckSubtypeAtResolution() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan)).contains(CardSubtype.DETECTIVE)
                .doesNotContain(CardSubtype.SCOUT);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @CardUsed({MaskwoodNexus.class})
    void scoutUpgradeReplacesAllExistingCreatureTypes() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.enterBattlefieldAndReturn(player1, new MaskwoodNexus());
        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan)).contains(CardSubtype.SCOUT, CardSubtype.DRAGON);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.DETECTIVE);
    }

    @Test
    @CardUsed({MaskwoodNexus.class})
    void detectiveUpgradeReplacesAllExistingCreatureTypes() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.enterBattlefieldAndReturn(player1, new MaskwoodNexus());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.ROGUE);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kellan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @CardUsed({MaskwoodNexus.class})
    void becomingScoutAgainAllowsGainingAnotherCombatDamageAbility() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new MaskwoodNexus());
        assertThat(gqs.effectiveCreatureSubtypes(gd, kellan)).contains(CardSubtype.SCOUT);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void rogueRetainsCombatDamageAbilityForBothDoubleStrikeHits() {
        addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(first.getId(), second.getId());
    }

    @Test
    void exiledLandCanBePlayedThisTurn() {
        addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
    }

    @Test
    void combatDamageWithEmptyLibraryDoesNotExileAnything() {
        addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }
}
