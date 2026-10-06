package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BoneDragon;
import com.github.laxika.magicalvibes.cards.d.DetectionTower;
import com.github.laxika.magicalvibes.cards.d.DismissivePyromancer;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfRenewal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunicArmasaur.class, Forest.class, ZuranSpellcaster.class, BoneDragon.class,
        DetectionTower.class, DismissivePyromancer.class, DruidOfTheCowl.class, FountainOfRenewal.class})
class RunicArmasaurTest extends BaseCardTest {

    @Test
    @DisplayName("May draw when an opponent activates a creature's non-mana ability")
    void mayDrawWhenOpponentActivatesCreatureNonManaAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        addCreatureReady(player2, new ZuranSpellcaster());
        harness.setHand(player1, List.of());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent activates a land's mana ability")
    void doesNotTriggerForLandManaAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the trigger does not draw")
    void decliningTriggerDoesNotDraw() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        addCreatureReady(player2, new ZuranSpellcaster());
        harness.setHand(player1, List.of());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("An opponent's land non-mana ability triggers a draw")
    void drawsForLandNonManaAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        harness.addToBattlefield(player2, new DetectionTower());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller's own creature activation does not trigger a draw")
    void doesNotTriggerForOwnCreatureAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        addCreatureReady(player1, new DismissivePyromancer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature mana ability does not trigger a draw")
    void doesNotTriggerForCreatureManaAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        addCreatureReady(player2, new DruidOfTheCowl());

        harness.tapPermanent(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature artifact's non-mana activation does not trigger a draw")
    void doesNotTriggerForArtifactAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        harness.addToBattlefield(player2, new FountainOfRenewal());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrificing the activated creature still triggers a draw before its damage resolves")
    void drawsBeforeSacrificedCreatureAbilityResolves() {
        Permanent armasaur = harness.addToBattlefieldAndReturn(player1, new RunicArmasaur());
        addCreatureReady(player2, new DismissivePyromancer());
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player2, 0, 1, null, armasaur.getId());

        harness.assertInGraveyard(player2, "Dismissive Pyromancer");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(armasaur.getMarkedDamage()).isZero();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Forest");

        harness.passBothPriorities();

        assertThat(armasaur.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Runic Armasaur");
    }

    @Test
    @DisplayName("A creature card activated in the graveyard does not trigger a draw")
    void doesNotTriggerForGraveyardAbility() {
        harness.addToBattlefield(player1, new RunicArmasaur());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player2, List.of(new BoneDragon(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bone Dragon");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
