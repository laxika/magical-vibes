package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HierophantsChalice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VraskaRelicSeeker.class, GrizzlyBears.class, GloriousAnthem.class, Forest.class, HierophantsChalice.class})
class VraskaRelicSeekerTest extends BaseCardTest {


    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VraskaRelicSeeker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Vraska on battlefield with loyalty 6")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new VraskaRelicSeeker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Vraska, Relic Seeker"));
        Permanent vraska = findVraska(player1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(vraska.isSummoningSick()).isFalse();
    }


    @Test
    @DisplayName("+2 creates a 2/2 black Pirate token with menace and increases loyalty")
    void plusTwoCreatesToken() {
        Permanent vraska = addReadyVraska(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(8); // 6 + 2

        Permanent token = findPermanent(player1, "Pirate");

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PIRATE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("+2 can be activated multiple turns in a row")
    void plusTwoCreatesMultipleTokens() {
        Permanent vraska = addReadyVraska(player1);

        // First activation
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);

        // Simulate new turn
        vraska.setLoyaltyActivationsThisTurn(0);

        // Second activation
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(10);

        long pirateCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Pirate"))
                .count();
        assertThat(pirateCount).isEqualTo(2);
    }


    @Test
    @DisplayName("-3 destroys target creature and creates a Treasure token")
    void minusThreeDestroysCreatureAndCreatesTreasure() {
        Permanent vraska = addReadyVraska(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 6 - 3

        // Bear should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Treasure token created for Vraska's controller
        Permanent treasure = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treasure"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Treasure token not found"));

        assertThat(treasure.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("-3 destroys target enchantment and creates a Treasure token")
    void minusThreeDestroysEnchantment() {
        Permanent vraska = addReadyVraska(player1);
        harness.addToBattlefield(player2, new GloriousAnthem());

        Permanent enchantment = findPermanent(player2, "Glorious Anthem");

        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 6 - 3

        // Enchantment should be destroyed
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");

        // Treasure token should exist
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treasure"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 rejects targeting a land")
    void minusThreeRejectsLand() {
        addReadyVraska(player1);
        harness.addToBattlefield(player2, new Forest());

        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 can target own permanent")
    void minusThreeCanTargetOwnPermanent() {
        Permanent vraska = addReadyVraska(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, ownBear.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 6 - 3

        // Own bear should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // Treasure token should still be created for controller
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treasure"))
                .count()).isEqualTo(1);
    }


    @Test
    @DisplayName("-10 sets target opponent's life total to 1")
    void minusTenSetsOpponentLifeToOne() {
        Permanent vraska = addReadyVraska(player1);
        vraska.setCounterCount(CounterType.LOYALTY, 10); // Need at least 10 loyalty

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeBefore).isEqualTo(GameData.STARTING_LIFE_TOTAL);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(0); // 10 - 10

        int lifeAfter = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeAfter).isEqualTo(1);

        // Vraska goes to graveyard at 0 loyalty
        harness.assertNotOnBattlefield(player1, "Vraska, Relic Seeker");
    }

    @Test
    @DisplayName("-10 can target self")
    void minusTenCanTargetSelf() {
        Permanent vraska = addReadyVraska(player1);
        vraska.setCounterCount(CounterType.LOYALTY, 10);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(1);
    }

    @Test
    @DisplayName("-10 cannot be activated without enough loyalty")
    void minusTenRequiresTenLoyalty() {
        addReadyVraska(player1);
        // Default loyalty is 6, not enough for -10

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("loyalty");
    }


    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyVraska(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyVraska(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }


    @Test
    @DisplayName("-3 destroys an artifact and its Treasure can immediately produce mana")
    void minusThreeDestroysArtifactAndTreasureProducesMana() {
        addReadyVraska(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HierophantsChalice());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hierophant's Chalice");
        harness.assertInGraveyard(player2, "Hierophant's Chalice");
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-3 creates no Treasure when its only target leaves before resolution")
    void minusThreeDoesNotCreateTreasureWithMissingTarget() {
        Permanent vraska = addReadyVraska(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HierophantsChalice());
        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-3 creates Treasure even when an indestructible target survives")
    void minusThreeCreatesTreasureWhenDestructionFails() {
        addReadyVraska(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HierophantsChalice());
        artifact.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hierophant's Chalice");
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("-3 rejects targeting a planeswalker")
    void minusThreeRejectsPlaneswalker() {
        addReadyVraska(player1);
        Permanent opponentVraska = harness.addToBattlefieldAndReturn(player2, new VraskaRelicSeeker());
        opponentVraska.setCounterCount(CounterType.LOYALTY, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentVraska.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-10 leaves a player already at one life at one and preserves excess loyalty")
    void minusTenAtOneLifeWithExcessLoyalty() {
        Permanent vraska = addReadyVraska(player1);
        vraska.setCounterCount(CounterType.LOYALTY, 11);
        harness.setLife(player2, 1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Vraska, Relic Seeker");
    }

    private Permanent addReadyVraska(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VraskaRelicSeeker());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent findVraska(Player player) {
        return findPermanent(player, "Vraska, Relic Seeker");
    }
}
