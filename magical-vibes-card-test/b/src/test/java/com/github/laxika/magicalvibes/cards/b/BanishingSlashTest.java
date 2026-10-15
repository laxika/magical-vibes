package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoldenTailDisciple;
import com.github.laxika.magicalvibes.cards.g.GenerousVisitor;
import com.github.laxika.magicalvibes.cards.r.RoaringEarth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishingSlash.class, GoldenTailDisciple.class, GenerousVisitor.class, BronzeCudgels.class,
        RoaringEarth.class})
class BanishingSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature and creates a vigilant Samurai when you control an artifact and enchantment")
    void destroysTappedCreatureAndCreatesSamurai() {
        harness.addToBattlefield(player1, new BronzeCudgels());
        harness.addToBattlefield(player1, new GoldenTailDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        target.tap();

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Generous Visitor");
        assertThat(findPermanents(player1, "Samurai")).singleElement().satisfies(samurai -> {
            assertThat(samurai.getCard().getPower()).isEqualTo(2);
            assertThat(samurai.getCard().getToughness()).isEqualTo(2);
            assertThat(samurai.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(samurai.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        });
    }

    @Test
    @DisplayName("Can resolve without choosing a target")
    void resolvesWithoutTarget() {
        harness.addToBattlefield(player1, new BronzeCudgels());
        harness.addToBattlefield(player1, new GoldenTailDisciple());

        cast(List.of());

        assertThat(findPermanents(player1, "Samurai")).hasSize(1);
    }

    @Test
    @DisplayName("Checks the artifact and enchantment condition after destruction")
    void checksConditionAfterDestruction() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BronzeCudgels());
        harness.addToBattlefield(player1, new GoldenTailDisciple());

        cast(List.of(artifact.getId()));

        harness.assertInGraveyard(player1, "Bronze Cudgels");
        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    @DisplayName("Rejects an untapped creature target")
    void rejectsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    void destroysNoncreatureEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoaringEarth());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Roaring Earth");
        harness.assertNotOnBattlefield(player2, "Roaring Earth");
    }

    @Test
    void rejectsChoosingTwoTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BronzeCudgels());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(artifact.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysUntappedEnchantmentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Golden-Tail Disciple");
        harness.assertNotOnBattlefield(player2, "Golden-Tail Disciple");
        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    void destroysOpponentsArtifactAndCreatesTokenForCaster() {
        harness.addToBattlefield(player1, new BronzeCudgels());
        harness.addToBattlefield(player1, new GoldenTailDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BronzeCudgels());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Bronze Cudgels");
        assertThat(findPermanents(player1, "Samurai")).hasSize(1);
        assertThat(findPermanents(player2, "Samurai")).isEmpty();
    }

    @Test
    void destroyingLastEnchantmentPreventsToken() {
        harness.addToBattlefield(player1, new BronzeCudgels());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenTailDisciple());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player1, "Golden-Tail Disciple");
        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    void requiresBothPermanentTypesUnderCastersControl() {
        harness.addToBattlefield(player1, new BronzeCudgels());
        harness.addToBattlefield(player2, new GoldenTailDisciple());

        cast(List.of());

        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    void enchantmentAloneDoesNotCreateToken() {
        harness.addToBattlefield(player1, new GoldenTailDisciple());

        cast(List.of());

        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    void untappingOnlyTargetPreventsAllEffects() {
        harness.addToBattlefield(player1, new BronzeCudgels());
        harness.addToBattlefield(player1, new GoldenTailDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        target.tap();
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Generous Visitor");
        harness.assertInGraveyard(player1, "Banishing Slash");
        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    void conditionIsCheckedAtResolutionRatherThanCasting() {
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();
        harness.castSorcery(player1, 0, List.of());

        harness.addToBattlefield(player1, new BronzeCudgels());
        harness.addToBattlefield(player1, new GoldenTailDisciple());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Samurai")).hasSize(1);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
