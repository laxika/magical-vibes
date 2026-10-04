package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({ElspethTirel.class, CarapaceForger.class, GlintHawkIdol.class, Plains.class})
class ElspethTirelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ElspethTirel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Elspeth Tirel");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 4")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new ElspethTirel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Elspeth Tirel"));
        Permanent elspeth = findPermanent(player1, "Elspeth Tirel");
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(elspeth.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+2 ability gains life equal to number of creatures controlled and increases loyalty")
    void plusTwoGainsLifeAndIncreasesLoyalty() {
        Permanent elspeth = addReadyElspeth(player1);
        // Add two creatures to player1's battlefield
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new CarapaceForger());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 4 + 2
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("+2 ability does not count opponent's creatures")
    void plusTwoDoesNotCountOpponentCreatures() {
        Permanent elspeth = addReadyElspeth(player1);
        // Add creatures only to opponent's battlefield
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.addToBattlefield(player2, new CarapaceForger());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 4 + 2
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore); // No life gained
    }

    @Test
    @DisplayName("+2 ability gains no life when controlling no creatures")
    void plusTwoGainsNoLifeWithNoCreatures() {
        addReadyElspeth(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        int lifeAfter = harness.getGameData().playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("-2 ability creates three 1/1 Soldier tokens and decreases loyalty")
    void minusTwoCreatesThreeTokens() {
        Permanent elspeth = addReadyElspeth(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 4 - 2

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        List<Permanent> soldiers = bf.stream()
                .filter(p -> p.getCard().getName().equals("Soldier")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1)
                .toList();
        assertThat(soldiers).hasSize(3);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        });
    }

    @Test
    @DisplayName("-5 ability destroys non-land non-token permanents but keeps Elspeth")
    void minusFiveDestroysOtherPermanentsButKeepsElspeth() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 6);

        // Add creatures to both sides
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player2, new CarapaceForger());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Elspeth should still be on the battlefield (she's the source — "other")
        harness.assertOnBattlefield(player1, "Elspeth Tirel");

        // Both Carapace Forger should be destroyed
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("-5 ability does not destroy tokens")
    void minusFiveDoesNotDestroyTokens() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 6);
        Permanent opponentElspeth = addReadyElspeth(player2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Soldier")).isEqualTo(3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Soldier")).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentElspeth);
        harness.assertInGraveyard(player2, "Elspeth Tirel");
        harness.assertOnBattlefield(player1, "Elspeth Tirel");
    }

    @Test
    @DisplayName("-5 ability preserves tokens on the battlefield")
    void minusFivePreservesTokens() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 7); // Enough for -2 then -5

        // First activate -2 to create tokens
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Verify tokens were created
        GameData gd = harness.getGameData();
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(3);

        // Add a non-token creature
        harness.addToBattlefield(player2, new CarapaceForger());

        // Reset loyalty ability usage so we can activate another loyalty ability
        elspeth.setLoyaltyActivationsThisTurn(0);

        // Now activate -5
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Tokens should survive
        long survivingTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(survivingTokens).isEqualTo(3);

        // Non-token creature should be destroyed
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("-5 ability does not destroy lands")
    void minusFiveDoesNotDestroyLands() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());

        // Count lands before
        GameData gd = harness.getGameData();
        long landsBefore = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long landsBeforeP2 = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();
        long landsAfter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long landsAfterP2 = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();

        assertThat(landsAfter).isEqualTo(landsBefore);
        assertThat(landsAfterP2).isEqualTo(landsBeforeP2);
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyElspeth(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during combat")
    void cannotActivateDuringCombat() {
        addReadyElspeth(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyElspeth(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Cannot use -5 when loyalty is only 4")
    void cannotActivateMinusFiveWithInsufficientLoyalty() {
        Permanent elspeth = addReadyElspeth(player1);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0")
    void diesWhenLoyaltyReachesZero() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 2);

        // -2 ability: 2 - 2 = 0, Elspeth dies to state-based actions
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Elspeth Tirel");
        harness.assertInGraveyard(player1, "Elspeth Tirel");
        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability still resolves after Elspeth dies to SBA at 0 loyalty")
    void abilityResolvesAfterDeath() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 2);

        // -2 ability: creates tokens even though Elspeth dies
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Tokens should have been created
        long soldierCount = findPermanents(player1, "Soldier").stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(soldierCount).isEqualTo(3);
    }

    @Test
    @DisplayName("+2 counts creatures when the ability resolves, not when activated")
    void plusTwoCountsCreaturesAtResolution() {
        addReadyElspeth(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new GlintHawkIdol());
        harness.addToBattlefield(player2, new CarapaceForger());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("+2 counts Soldier creature tokens")
    void plusTwoCountsTokens() {
        Permanent elspeth = addReadyElspeth(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        elspeth.setLoyaltyActivationsThisTurn(0);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    @DisplayName("-5 destroys noncreature artifacts on both battlefields")
    void minusFiveDestroysNoncreatureArtifacts() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player1, new GlintHawkIdol());
        harness.addToBattlefield(player2, new GlintHawkIdol());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glint Hawk Idol");
        harness.assertNotOnBattlefield(player2, "Glint Hawk Idol");
        harness.assertInGraveyard(player1, "Glint Hawk Idol");
        harness.assertInGraveyard(player2, "Glint Hawk Idol");
    }

    @Test
    @DisplayName("-5 resolves after paying the last five loyalty counters")
    void minusFiveResolvesAfterSourceDies() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player2, new CarapaceForger());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertInGraveyard(player1, "Elspeth Tirel");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player1, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    private Permanent addReadyElspeth(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ElspethTirel());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
