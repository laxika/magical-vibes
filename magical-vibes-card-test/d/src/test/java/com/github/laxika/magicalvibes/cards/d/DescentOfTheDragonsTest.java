package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SarkhansRage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DescentOfTheDragons.class, GrizzlyBears.class, Forest.class,
        ColossodonYearling.class, DragonFodder.class, DragonlordSilumgar.class, SarkhansRage.class})
class DescentOfTheDragonsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creatures and creates a Dragon for each creature's controller")
    void destroysCreaturesAndCreatesDragonsForTheirControllers() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDescent(List.of(ownBear.getId(), opponentBear.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(dragons(player1)).hasSize(1).allSatisfy(this::assertDragon);
        assertThat(dragons(player2)).hasSize(1).allSatisfy(this::assertDragon);
    }

    @Test
    @DisplayName("Creates no Dragons when no creatures are targeted")
    void resolvesWithNoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDescent(List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(dragons(player1)).isEmpty();
        assertThat(dragons(player2)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures");
    }

    @Test
    @DisplayName("Can destroy more than ninety-nine target creatures")
    void destroysOneHundredCreatures() {
        List<UUID> targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new ColossodonYearling()).getId())
                .toList();

        castDescent(targets);

        harness.assertNotOnBattlefield(player2, "Colossodon Yearling");
        assertThat(dragons(player2)).hasSize(100).allSatisfy(this::assertDragon);
        assertThat(dragons(player1)).isEmpty();
    }

    @Test
    @DisplayName("Destroyed token creatures each create a Dragon")
    void destroysTokensAndCreatesOneDragonForEach() {
        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        List<UUID> goblins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Goblin"))
                .map(Permanent::getId)
                .toList();
        assertThat(goblins).hasSize(2);

        castDescent(goblins);

        harness.assertNotOnBattlefield(player1, "Goblin");
        assertThat(dragons(player1)).hasSize(2).allSatisfy(this::assertDragon);
        assertThat(dragons(player2)).isEmpty();
    }

    @Test
    @DisplayName("An indestructible creature survives and creates no Dragon")
    void indestructibleCreatureDoesNotCreateDragon() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        protectedCreature.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());

        castDescent(List.of(protectedCreature.getId(), ordinaryCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(protectedCreature)
                .doesNotContain(ordinaryCreature);
        assertThat(dragons(player2)).hasSize(1).allSatisfy(this::assertDragon);
    }

    @Test
    @DisplayName("A regenerated creature survives and creates no Dragon")
    void regeneratedCreatureDoesNotCreateDragon() {
        Permanent regeneratingCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        regeneratingCreature.setRegenerationShield(1);
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());

        castDescent(List.of(regeneratingCreature.getId(), ordinaryCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(regeneratingCreature)
                .doesNotContain(ordinaryCreature);
        assertThat(regeneratingCreature.isTapped()).isTrue();
        assertThat(regeneratingCreature.getRegenerationShield()).isZero();
        assertThat(dragons(player2)).hasSize(1).allSatisfy(this::assertDragon);
    }

    @Test
    @DisplayName("A target removed before resolution creates no Dragon while remaining targets resolve")
    void removedTargetDoesNotCreateDragon() {
        Permanent removedCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent remainingCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        prepareCast();
        harness.castSorcery(player1, 0, List.of(removedCreature.getId(), remainingCreature.getId()));
        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, removedCreature.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Colossodon Yearling");
        assertThat(dragons(player2)).hasSize(1).allSatisfy(this::assertDragon);
        assertThat(dragons(player1)).isEmpty();
    }

    @Test
    @DisplayName("A stolen creature creates a Dragon for its controller, even when the control source also dies")
    void usesControllerBeforeSimultaneousDestruction() {
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new DragonlordSilumgar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, stolenCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Colossodon Yearling");
        Permanent silumgar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DragonlordSilumgar)
                .findFirst().orElseThrow();

        castDescent(List.of(silumgar.getId(), stolenCreature.getId()));

        harness.assertInGraveyard(player1, "Dragonlord Silumgar");
        harness.assertInGraveyard(player2, "Colossodon Yearling");
        assertThat(dragons(player1)).hasSize(2).allSatisfy(this::assertDragon);
        assertThat(dragons(player2)).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void rejectsDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDescent(List<UUID> targetIds) {
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DescentOfTheDragons()));
        harness.addMana(player1, ManaColor.RED, 6);
    }

    private List<Permanent> dragons(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Dragon"))
                .toList();
    }

    private void assertDragon(Permanent dragon) {
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(dragon.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
