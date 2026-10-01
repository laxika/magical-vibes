package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({AjaniGoldmane.class, AnointerPriest.class, WoodlandChangeling.class})
class AjaniGoldmaneTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AjaniGoldmane()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(AjaniGoldmane.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 4")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new AjaniGoldmane()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard() instanceof AjaniGoldmane);
        Permanent ajani = bf.stream().filter(p -> p.getCard() instanceof AjaniGoldmane).findFirst().orElseThrow();
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(ajani.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: You gain 2 life =====

    @Test
    @DisplayName("+1 ability gains 2 life and increases loyalty")
    void plusOneGainsLifeAndIncreasesLoyalty() {
        Permanent ajani = addReadyAjani(player1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 4 + 1
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore + 2);
    }

    // ===== -1 ability: Put +1/+1 counters and grant vigilance =====

    @Test
    @DisplayName("-1 ability puts +1/+1 counter on each creature and grants vigilance")
    void minusOnePutsCountersAndGrantsVigilance() {
        Permanent ajani = addReadyAjani(player1);
        Permanent firstCreature = addCreatureReady(player1, new WoodlandChangeling());
        Permanent secondCreature = addCreatureReady(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 4 - 1

        // Each creature should have a +1/+1 counter
        for (Permanent creature : List.of(firstCreature, secondCreature)) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        }
    }

    @Test
    @DisplayName("-1 ability does not affect opponent's creatures")
    void minusOneDoesNotAffectOpponentCreatures() {
        addReadyAjani(player1);
        Permanent opponentCreature = addCreatureReady(player2, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("-1 counters are permanent, vigilance is until end of turn")
    void minusOneCountersArePermanentVigilanceIsTemporary() {
        addReadyAjani(player1);
        Permanent creature = addCreatureReady(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // +1/+1 counter is permanent
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        // Effective P/T: base 2/2 + 1 counter = 3/3
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("-1 ability does not affect creatures entering after it resolves")
    void minusOneDoesNotAffectCreaturesEnteringLater() {
        addReadyAjani(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent creatureEnteringLater = addCreatureReady(player1, new WoodlandChangeling());

        assertThat(creatureEnteringLater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creatureEnteringLater, Keyword.VIGILANCE)).isFalse();
    }

    // ===== -6 ability: Create Avatar token =====

    @Test
    @DisplayName("-6 ability creates Avatar token with P/T equal to life total")
    void minusSixCreatesAvatarToken() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Find the Avatar token
        Permanent avatar = findAvatarToken();

        assertThat(gqs.isCreature(gd, avatar)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, avatar)).containsExactly(CardColor.WHITE);
        assertThat(avatar.getCard().getSubtypes()).containsExactly(CardSubtype.AVATAR);

        // P/T should equal controller's life total (20 default)
        int lifeTotal = gd.playerLifeTotals.get(player1.getId());
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(lifeTotal);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(lifeTotal);
    }

    @Test
    @DisplayName("Avatar token P/T changes when life total changes")
    void avatarTokenPTChangesWithLifeTotal() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent avatar = findAvatarToken();

        // Default life is 20
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(20);

        // Change life total to 10
        harness.setLife(player1, 10);
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(10);
    }

    @Test
    @DisplayName("Avatar token entering fires ally creature-enters triggers")
    void avatarTokenFiresEnterTriggers() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player1, new AnointerPriest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();

        // Anointer Priest: "whenever a creature token you control enters, you gain 1 life".
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyAjani(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during combat")
    void cannotActivateDuringCombat() {
        addReadyAjani(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyAjani(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Cannot use -6 when loyalty is only 4")
    void cannotActivateMinusSixWithInsufficientLoyalty() {
        Permanent ajani = addReadyAjani(player1);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Planeswalker dies at 0 loyalty =====

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0 but ability still resolves")
    void diesWhenLoyaltyReachesZeroAbilityStillResolves() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 1);
        Permanent creature = addCreatureReady(player1, new WoodlandChangeling());

        // -1 ability: 1 - 1 = 0, Ajani dies to state-based actions
        harness.activateAbility(player1, 0, 1, null, null);

        // Ajani should be in the graveyard, but its ability remains on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof AjaniGoldmane);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AjaniGoldmane);

        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve - effects should still apply
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        // The creature should still have gotten the +1/+1 counter.
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyAjani(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniGoldmane());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent findAvatarToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.AVATAR))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Avatar token not found"));
    }
}
