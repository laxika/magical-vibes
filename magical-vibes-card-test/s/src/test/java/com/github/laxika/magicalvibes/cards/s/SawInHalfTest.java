package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawInHalf.class, GrizzlyBears.class, Mountain.class, LeylineOfTheVoid.class, Maro.class})
class SawInHalfTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates two token copies with half its current power and toughness")
    void destroysCreatureAndCreatesTwoHalfSizedCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castSawInHalf(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        List<Permanent> copies = findPermanents(player2, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy -> {
            assertThat(copy.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.BEAR);
            assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
            assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        });
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Creates no copies when regeneration prevents the creature from dying")
    void doesNotCreateCopiesWhenTargetRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        castSawInHalf(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).noneMatch(card -> card.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SawInHalf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void doesNotCreateCopiesWhenCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSawInHalf(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    void copiesFaceDownCharacteristicsRatherThanUnderlyingCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        castSawInHalf(target);

        harness.assertInGraveyard(player2, "Mountain");
        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(gqs.isLand(gd, token)).isFalse();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    void doesNotCreateCopiesOfIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castSawInHalf(target);

        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(target);
        assertThat(target.getCard().isToken()).isFalse();
    }

    @Test
    void canDestroyTokenAndCopyItsPreviouslyOverriddenPowerAndToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castSawInHalf(target);
        Permanent token = findPermanents(player2, "Grizzly Bears").getFirst();

        castSawInHalf(token);

        List<Permanent> copies = findPermanents(player2, "Grizzly Bears");
        assertThat(copies).hasSize(3);
        assertThat(copies).allMatch(copy -> copy.getCard().isToken());
        assertThat(copies.stream().map(copy -> gqs.getEffectivePower(gd, copy)).toList())
                .containsExactlyInAnyOrder(3, 2, 2);
        assertThat(copies.stream().map(copy -> gqs.getEffectiveToughness(gd, copy)).toList())
                .containsExactlyInAnyOrder(3, 2, 2);
    }

    @Test
    void copiesHaveFixedPowerAndToughnessInsteadOfCharacteristicDefiningAbility() {
        harness.setHand(player2, List.of(new Mountain(), new Mountain(), new Mountain()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Maro());

        castSawInHalf(target);
        harness.setHand(player2, List.of());

        List<Permanent> copies = findPermanents(player2, "Maro");
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy -> {
            assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        });
    }

    private void castSawInHalf(Permanent target) {
        harness.setHand(player1, List.of(new SawInHalf()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
