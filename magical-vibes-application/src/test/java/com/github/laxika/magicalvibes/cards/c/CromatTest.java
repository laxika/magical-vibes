package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BloodfireColossus;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cromat.class, BloodfireColossus.class, GaeasSkyfolk.class})
class CromatTest extends BaseCardTest {

    @Test
    @DisplayName("Flying and the power boost last until end of turn")
    void flyingAndBoostLastUntilEndOfTurn() {
        Permanent cromat = addReadyCromat(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cromat, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cromat)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cromat, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, cromat)).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroys a creature blocking or blocked by Cromat")
    void destroysCreatureInCombatWithCromat() {
        Permanent cromat = addReadyCromat(player1);
        Permanent attacker = addCreatureReady(player2, new BloodfireColossus());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cromat);
    }

    @Test
    @DisplayName("Destroys a creature blocking Cromat when Cromat is attacking")
    void destroysCreatureBlockingAttackingCromat() {
        Permanent cromat = addReadyCromat(player1);
        Permanent blocker = addCreatureReady(player2, new BloodfireColossus());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cromat);
    }

    @Test
    @DisplayName("Cannot target a creature that is not in combat with Cromat")
    void combatAbilityRejectsCreatureOutsideCombat() {
        addReadyCromat(player1);
        Permanent creature = addCreatureReady(player2, new BloodfireColossus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking or blocked");
    }

    @Test
    @DisplayName("Regeneration lets Cromat survive lethal combat damage")
    void regenerationPreventsLethalCombatDamage() {
        Permanent cromat = addReadyCromat(player1);
        addCreatureReady(player2, new BloodfireColossus());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cromat);
        assertThat(cromat.isTapped()).isTrue();
        assertThat(cromat.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Puts Cromat on top of its owner's library")
    void putsCromatOnTopOfLibrary() {
        Permanent cromat = addReadyCromat(player1);
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cromat);
        assertThat(library).hasSize(2);
        assertThat(library.getFirst()).isSameAs(cromat.getCard());
    }

    @Test
    @DisplayName("Repeated boosts increase both power and toughness until end of turn")
    void repeatedBoostsAccumulateAndExpire() {
        Permanent cromat = addReadyCromat(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cromat)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, cromat)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cromat)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cromat)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creating a regeneration shield does not tap Cromat or remove it from combat")
    void regenerationShieldDoesNotImmediatelyRemoveCromatFromCombat() {
        Permanent cromat = addReadyCromat(player1);
        addCreatureReady(player2, new BloodfireColossus());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(cromat.isTapped()).isFalse();
        assertThat(cromat.isBlocking()).isTrue();
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cromat);
        assertThat(cromat.isTapped()).isTrue();
        assertThat(cromat.isBlocking()).isFalse();
        assertThat(cromat.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A borrowed Cromat goes to its owner's library rather than its controller's")
    void borrowedCromatReturnsToOwnersLibrary() {
        Cromat card = new Cromat();
        card.setOwnerId(player2.getId());
        Permanent cromat = addCreatureReady(player1, card);
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        harness.setLibrary(player2, List.of(new GaeasSkyfolk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cromat);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(card);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).doesNotContain(card);
    }

    private Permanent addReadyCromat(Player player) {
        return addCreatureReady(player, new Cromat());
    }
}
