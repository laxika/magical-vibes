package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Conviction;
import com.github.laxika.magicalvibes.cards.c.CountlessGearsRenegade;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SpearOfHeliod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestorationSpecialist.class, Ornithopter.class, Conviction.class, CountlessGearsRenegade.class})
class RestorationSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one artifact and up to one enchantment from the graveyard")
    void returnsArtifactAndEnchantment() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        Card enchantment = new Conviction();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId(), enchantment.getId()));
        harness.assertNotOnBattlefield(player1, "Restoration Specialist");
        harness.assertInGraveyard(player1, "Restoration Specialist");
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Conviction");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInHand(player1, "Conviction");
        harness.assertInGraveyard(player1, "Restoration Specialist");
    }

    @Test
    @DisplayName("May sacrifice itself without choosing targets")
    void allowsNoTargets() {
        Permanent specialist = addSpecialist();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, specialistIndex(specialist), 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Restoration Specialist");
    }

    @Test
    @DisplayName("Rejects two artifact targets")
    void rejectsTwoArtifactTargets() {
        Permanent specialist = addSpecialist();
        Card firstArtifact = new Ornithopter();
        Card secondArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one artifact");
    }

    @Test
    @DisplayName("Rejects a creature target")
    void rejectsCreatureTarget() {
        Permanent specialist = addSpecialist();
        Card creature = new CountlessGearsRenegade();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsOnlyArtifactWhenEnchantmentIsNotChosen() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        Card enchantment = new Conviction();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Conviction");
        harness.assertNotInHand(player1, "Conviction");
    }

    @Test
    void returnsOnlyEnchantmentWhenArtifactIsNotChosen() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        Card enchantment = new Conviction();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Conviction");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
    }

    @Test
    void rejectsTwoEnchantmentTargets() {
        Permanent specialist = addSpecialist();
        Card first = new Conviction();
        Card second = new Conviction();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Restoration Specialist");
    }

    @Test
    void rejectsTargetsInOpponentsGraveyard() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Restoration Specialist");
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        Permanent specialist = addSpecialist();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Restoration Specialist");
        harness.assertNotInGraveyard(player1, "Restoration Specialist");
    }

    @Test
    void returnsRemainingTargetWhenOtherTargetLeavesGraveyard() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        Card enchantment = new Conviction();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId(), enchantment.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(artifact);
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertInHand(player1, "Conviction");
        harness.assertInGraveyard(player1, "Restoration Specialist");
    }

    @Test
    @CardUsed({SpearOfHeliod.class})
    void returnsArtifactAndArtifactEnchantment() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        Card enchantment = new SpearOfHeliod();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInHand(player1, "Spear of Heliod");
    }

    @Test
    @CardUsed({SpearOfHeliod.class})
    void returnsArtifactEnchantmentAndEnchantment() {
        Permanent specialist = addSpecialist();
        Card artifact = new SpearOfHeliod();
        Card enchantment = new Conviction();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spear of Heliod");
        harness.assertInHand(player1, "Conviction");
    }

    @Test
    @CardUsed({SpearOfHeliod.class})
    void canChooseSameArtifactEnchantmentForBothTargets() {
        Permanent specialist = addSpecialist();
        Card spear = new SpearOfHeliod();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spear));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(spear.getId(), spear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spear);
        harness.assertNotInGraveyard(player1, "Spear of Heliod");
    }

    private Permanent addSpecialist() {
        return harness.addToBattlefieldAndReturn(player1, new RestorationSpecialist());
    }

    private int specialistIndex(Permanent specialist) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(specialist);
    }
}
