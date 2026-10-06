package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.r.RerouteSystems;
import com.github.laxika.magicalvibes.cards.h.Hylderblade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedshipImpact.class, FountainOfYouth.class, Bitterblossom.class, RodOfRuin.class,
        GrizzlyBears.class, Hylderblade.class, Island.class, RerouteSystems.class})
class SeedshipImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a low-mana-value artifact and creates a Lander")
    void destroysLowManaValueArtifactAndCreatesLander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(target);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Destroys a low-mana-value enchantment and creates a Lander")
    void destroysLowManaValueEnchantmentAndCreatesLander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bitterblossom());

        cast(target);

        harness.assertInGraveyard(player2, "Bitterblossom");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Lander for a permanent with mana value greater than 2")
    void doesNotCreateLanderForHighManaValuePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        cast(target);

        harness.assertInGraveyard(player2, "Rod of Ruin");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSeedshipImpact();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    void createsLanderEvenWhenLowManaValueTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Hylderblade());
        harness.setHand(player1, List.of(new RerouteSystems()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        cast(target);

        harness.assertOnBattlefield(player2, "Hylderblade");
        harness.assertNotInGraveyard(player2, "Hylderblade");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void newlyCreatedLanderFindsBasicLandTapped() {
        cast(harness.addToBattlefieldAndReturn(player2, new Hylderblade()));
        Island island = new Island();
        harness.setLibrary(player1, List.of(new SeedshipImpact(), island));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Island");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Island").getCard()).isSameAs(island);
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof SeedshipImpact);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landerCanBeSacrificedWithoutFindingBasicLand() {
        cast(harness.addToBattlefieldAndReturn(player2, new Hylderblade()));
        harness.setLibrary(player1, List.of(new SeedshipImpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void destroyingOwnLanderCreatesReplacementLander() {
        cast(harness.addToBattlefieldAndReturn(player2, new Hylderblade()));
        Permanent original = findPermanent(player1, "Lander");

        cast(original);

        assertThat(findPermanents(player1, "Lander")).hasSize(1)
                .doesNotContain(original);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void createsNoLanderWhenTargetIsSacrificedBeforeResolution() {
        cast(harness.addToBattlefieldAndReturn(player2, new Hylderblade()));
        Permanent lander = findPermanent(player1, "Lander");
        harness.setLibrary(player1, List.of(new SeedshipImpact()));
        prepareSeedshipImpact();
        harness.castInstant(player1, 0, lander.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(lander), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Seedship Impact");
    }

    private void cast(Permanent target) {
        prepareSeedshipImpact();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareSeedshipImpact() {
        harness.setHand(player1, List.of(new SeedshipImpact()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
