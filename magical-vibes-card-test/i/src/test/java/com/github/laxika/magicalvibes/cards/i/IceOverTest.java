package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Puppeteer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceOver.class, FountainOfYouth.class, GrizzlyBears.class, Island.class})
class IceOverTest extends BaseCardTest {

    @Test
    @DisplayName("Ice Over can target a creature")
    void canTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castIceOver(creature);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ice Over can target an artifact")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castIceOver(artifact);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ice Over cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new IceOver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Enchanted permanent does not untap during its controller's untap step")
    void enchantedPermanentDoesNotUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.tap();

        Permanent aura = new Permanent(new IceOver());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted permanent untaps after Ice Over leaves the battlefield")
    void enchantedPermanentUntapsAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.tap();

        Permanent aura = new Permanent(new IceOver());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private void castIceOver(Permanent target) {
        harness.setHand(player1, List.of(new IceOver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving Ice Over does not tap an untapped permanent")
    void resolvingAuraDoesNotTapPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castIceOver(creature);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(aura -> {
                    assertThat(aura.getCard()).isInstanceOf(IceOver.class);
                    assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
                });
    }

    @Test
    @DisplayName("A resolved Ice Over keeps an artifact tapped but allows other permanents to untap")
    void enchantedArtifactDoesNotUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        artifact.tap();
        otherArtifact.tap();
        castIceOver(artifact);
        harness.passBothPriorities();

        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(otherArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ice Over also prevents its controller's own enchanted creature from untapping")
    void canEnchantOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        castIceOver(creature);
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @CardUsed({Puppeteer.class})
    @DisplayName("Ice Over allows its enchanted creature to be untapped by an ability")
    void enchantedCreatureCanUntapOutsideUntapStep() {
        Permanent puppeteer = harness.addToBattlefieldAndReturn(player1, new Puppeteer());
        puppeteer.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        castIceOver(creature);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ice Over");
    }

    @Test
    @DisplayName("Ice Over goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castIceOver(creature);
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ice Over");
        harness.assertInGraveyard(player1, "Ice Over");
    }
}
