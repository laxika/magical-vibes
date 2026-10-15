package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShurisFabricator.class, Spellbook.class, GrizzlyBears.class, DoublingSeason.class})
class ShurisFabricatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two tapped indestructible Vibranium tokens")
    void entersWithVibraniumTokens() {
        harness.setHand(player1, List.of(new ShurisFabricator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        List<Permanent> vibranium = findPermanents(player1, "Vibranium");
        assertThat(vibranium).hasSize(2);
        assertThat(vibranium).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        });
    }

    @Test
    @DisplayName("Vibranium produces restricted colorless mana")
    void vibraniumProducesPowerstoneMana() {
        harness.setHand(player1, List.of(new ShurisFabricator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent vibranium = findPermanents(player1, "Vibranium").getFirst();
        vibranium.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Returns a target artifact from the graveyard with a finality counter")
    void returnsArtifactWithFinalityCounter() {
        Permanent fabricator = harness.addToBattlefieldAndReturn(player1, new ShurisFabricator());
        fabricator.setSummoningSick(false);
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Spellbook");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot target a nonartifact card in the graveyard")
    void cannotTargetNonartifact() {
        Permanent fabricator = harness.addToBattlefieldAndReturn(player1, new ShurisFabricator());
        fabricator.setSummoningSick(false);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void finalityCounterIsDoubledByReplacementEffect() {
        harness.addToBattlefield(player1, new ShurisFabricator());
        harness.addToBattlefield(player1, new DoublingSeason());
        Card artifact = new ShurisFabricator();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(artifact.getId())).findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(2);
        assertThat(findPermanents(player1, "Vibranium")).hasSize(4);
    }

    @Test
    void finalityExilesReturnedArtifactWhenSacrificed() {
        harness.addToBattlefield(player1, new ShurisFabricator());
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Spellbook");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
        assertThat(gd.findExiledCard(artifact.getId())).isNotNull();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new ShurisFabricator());
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsArtifact() {
        harness.addToBattlefield(player1, new ShurisFabricator());
        Card artifact = new Spellbook();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyard() {
        Permanent fabricator = harness.addToBattlefieldAndReturn(player1, new ShurisFabricator());
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(artifact));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(fabricator.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void vibraniumManaCannotCastNonartifactCreature() {
        harness.setHand(player1, List.of(new ShurisFabricator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent vibranium = findPermanents(player1, "Vibranium").getFirst();
        vibranium.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void vibraniumManaCanPayForArtifactReturnAbility() {
        harness.setHand(player1, List.of(new ShurisFabricator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        for (Permanent vibranium : findPermanents(player1, "Vibranium")) {
            vibranium.untap();
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);
        }
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }
}
