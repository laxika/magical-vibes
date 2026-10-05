package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.c.CathedralSanctifier;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfiniteReflection.class, Archangel.class, WanderingWolf.class,
        ThatcherRevolt.class, NaturalEnd.class, CathedralSanctifier.class})
class InfiniteReflectionTest extends BaseCardTest {

    private void castReflectionOn(UUID targetId) {
        harness.setHand(player1, List.of(new InfiniteReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // Aura resolves and attaches
        harness.passBothPriorities(); // ETB trigger resolves
    }

    @Test
    @DisplayName("On entering, each other nontoken creature you control becomes a copy of the enchanted creature")
    void otherNontokenCreaturesBecomeCopies() {
        harness.addToBattlefield(player1, new Archangel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());

        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
        assertThat(bears.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected by the enters trigger")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new Archangel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());

        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        assertThat(bears.getCard().getName()).isEqualTo("Wandering Wolf");
        assertThat(bears.getCard().getPower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken creatures cast later enter as a copy of the enchanted creature")
    void laterNontokenCreatureEntersAsCopy() {
        harness.addToBattlefield(player1, new Archangel());
        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Wandering Wolf"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tokens enter as themselves")
    void tokensEnterAsThemselves() {
        harness.addToBattlefield(player1, new Archangel());
        castReflectionOn(harness.getPermanentId(player1, "Archangel"));
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList())
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Human");
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Creatures entering after the Aura leaves are unaffected")
    void effectStopsAfterAuraLeaves() {
        harness.addToBattlefield(player1, new Archangel());
        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Infinite Reflection"));

        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Wandering Wolf"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().getName()).isEqualTo("Wandering Wolf");
        assertThat(bears.getCard().getPower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The enters trigger still copies creatures if the Aura is destroyed in response")
    void triggerResolvesAfterAuraIsDestroyed() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Archangel());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new InfiniteReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new NaturalEnd()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Infinite Reflection"));
        harness.assertNotOnBattlefield(player1, "Infinite Reflection");
        harness.passBothPriorities();

        assertThat(wolf.getCard().getName()).isEqualTo("Archangel");
        assertThat(wolf.getCard().getPower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Copies already made remain copies after the Aura is destroyed")
    void existingCopiesPersistAfterAuraLeaves() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Archangel());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        castReflectionOn(enchanted.getId());

        harness.setHand(player1, List.of(new NaturalEnd()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Infinite Reflection"));

        assertThat(wolf.getCard().getName()).isEqualTo("Archangel");
        assertThat(wolf.getCard().getPower()).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's creature can be enchanted without copying the opponent's other creatures")
    void canCopyOpponentsEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new Archangel());
        Permanent opposingWolf = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        Permanent ownWolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());

        castReflectionOn(enchanted.getId());

        assertThat(ownWolf.getCard().getName()).isEqualTo("Archangel");
        assertThat(opposingWolf.getCard().getName()).isEqualTo("Wandering Wolf");
    }

    @Test
    @DisplayName("Tokens already present are excluded from the enters trigger")
    void existingTokensAreNotCopied() {
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Archangel());

        castReflectionOn(enchanted.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList())
                .hasSize(3)
                .allSatisfy(token -> assertThat(token.getCard().getName()).isEqualTo("Human"));
    }

    @Test
    @DisplayName("Copying a token does not make the other creatures tokens")
    void copyingTokenPreservesNontokenStatus() {
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());

        castReflectionOn(harness.getPermanentId(player1, "Human"));

        assertThat(wolf.getCard().getName()).isEqualTo("Human");
        assertThat(wolf.getCard().isToken()).isFalse();
        assertThat(wolf.getCard().getPower()).isEqualTo(1);
        assertThat(wolf.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Later creatures trigger the copied creature's enters ability")
    void laterCreatureUsesCopiedEntersAbility() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        castReflectionOn(enchanted.getId());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A creature's original enters ability does not trigger when it enters as a copy")
    void originalEntersAbilityDoesNotTrigger() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Archangel());
        castReflectionOn(enchanted.getId());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CathedralSanctifier()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Cathedral Sanctifier")))
                .singleElement().satisfies(copy -> assertThat(copy.getCard().getName()).isEqualTo("Archangel"));
    }

    @Test
    @DisplayName("With two distinct enchanted creatures, the controller can choose the replacement order")
    void multipleReflectionsOfferReplacementOrderChoice() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new Archangel());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        castReflectionOn(angel.getId());
        castReflectionOn(wolf.getId());
        harness.setHand(player1, List.of(new CathedralSanctifier()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingInteractions).isNotEmpty();
    }

    @Test
    @DisplayName("Opponent's later creatures enter as themselves")
    void opponentLaterCreatureIsNotCopied() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Archangel());
        castReflectionOn(enchanted.getId());
        harness.setHand(player2, List.of(new WanderingWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wandering Wolf");
        harness.assertNotOnBattlefield(player2, "Archangel");
    }

    @Test
    @DisplayName("Copy effects do not copy the enchanted creature's counters or tapped status")
    void copiesIgnoreCountersAndTappedStatus() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Archangel());
        enchanted.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        enchanted.tap();
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());

        castReflectionOn(enchanted.getId());

        assertThat(wolf.getCard().getPower()).isEqualTo(5);
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(wolf.isTapped()).isFalse();
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Wandering Wolf")).toList())
                .hasSize(2)
                .allSatisfy(copy -> {
                    assertThat(copy.getCard().getPower()).isEqualTo(5);
                    assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(copy.isTapped()).isFalse();
                });
    }

    @Test
    @DisplayName("Existing creatures becoming copies do not trigger copied enters abilities")
    void becomingCopyDoesNotEnterAgain() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setLife(player1, 20);

        castReflectionOn(enchanted.getId());

        assertThat(wolf.getCard().getName()).isEqualTo("Cathedral Sanctifier");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
