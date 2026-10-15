package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FadeIntoAntiquity;
import com.github.laxika.magicalvibes.cards.f.FlameDischarge;
import com.github.laxika.magicalvibes.model.CardColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarmoniousEmergence.class, Demolish.class, Forest.class, FadeIntoAntiquity.class, FlameDischarge.class})
class HarmoniousEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 4/5 green Spirit creature with vigilance and haste")
    void animatesEnchantedLand() {
        Permanent forest = addEnchantedForest();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("When enchanted land would be destroyed, the Aura is sacrificed and the land gains indestructible")
    void sacrificesAuraAndGrantsIndestructible() {
        Permanent forest = addEnchantedForest();

        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Harmonious Emergence");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("Harmonious Emergence can enchant only a land controlled by its caster")
    void targetMustBeLandYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentForest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new HarmoniousEmergence()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    void castingAuraAnimatesOwnLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HarmoniousEmergence()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Harmonious Emergence").getAttachedTo()).isEqualTo(forest.getId());
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
    }

    @Test
    void exilingAuraEndsAnimationWithoutGrantingIndestructible() {
        Permanent forest = addEnchantedForest();
        Permanent aura = findPermanent(player1, "Harmonious Emergence");
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Harmonious Emergence");
        harness.assertNotInGraveyard(player1, "Harmonious Emergence");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aura.getCard());
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
    }

    @Test
    void replacementProtectsAgainstFurtherDestructionOnlyUntilEndOfTurn() {
        Permanent forest = addEnchantedForest();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Demolish(), new Demolish()));
        harness.addMana(player2, ManaColor.RED, 8);

        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();
        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setLibrary(player1, List.of(new Forest()));
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void lethalDamageSacrificesAuraAndPreservesLand() {
        Permanent forest = addEnchantedForest();
        harness.setHand(player2, List.of(new FlameDischarge()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstantForX(player2, 0, 5, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Harmonious Emergence");
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void sacrificingLandDoesNotApplyDestructionReplacement() {
        Permanent forest = addEnchantedForest();

        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, forest);
        harness.getPermanentRemovalService().removeOrphanedAuras(gd);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Harmonious Emergence");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void controllerChoosesWhichAuraReplacesDestruction() {
        Permanent forest = addEnchantedForest();
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new HarmoniousEmergence());
        secondAura.setAttachedTo(forest.getId());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertNotInGraveyard(player1, "Harmonious Emergence");
    }

    private Permanent addEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HarmoniousEmergence());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
