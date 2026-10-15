package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DogmeatEverLoyal;
import com.github.laxika.magicalvibes.cards.j.JunkJet;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommanderSofiaDaguerre.class, JunkJet.class, DogmeatEverLoyal.class})
class CommanderSofiaDaguerreTest extends BaseCardTest {

    @Test
    void destroysLegendaryPermanentAndCreatesJunkForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DogmeatEverLoyal());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Dogmeat, Ever Loyal");
        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(countPermanents(player2, "Junk")).isEqualTo(1);
        assertThat(findPermanent(player2, "Junk").getCard().getSubtypes())
                .containsExactly(CardSubtype.JUNK);
    }

    @Test
    void canChooseNoTarget() {
        cast(List.of());

        harness.assertOnBattlefield(player1, "Commander Sofia Daguerre");
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void cannotTargetNonlegendaryPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JunkJet());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary");
    }

    @Test
    void createsJunkForOwnLegendaryPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DogmeatEverLoyal());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player1, "Dogmeat, Ever Loyal");
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        assertThat(countPermanents(player2, "Junk")).isZero();
    }

    @Test
    void createsJunkEvenWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DogmeatEverLoyal());
        target.setCounterCount(CounterType.INDESTRUCTIBLE, 1);

        cast(List.of(target.getId()));

        harness.assertOnBattlefield(player2, "Dogmeat, Ever Loyal");
        assertThat(countPermanents(player2, "Junk")).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void doesNotCreateJunkWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DogmeatEverLoyal());
        prepareCast();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Commander Sofia Daguerre");
        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(countPermanents(player2, "Junk")).isZero();
    }

    @Test
    void junkSacrificesToExileTopCardAndRequiresManaToPlayIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DogmeatEverLoyal());
        cast(List.of(target.getId()));
        JunkJet top = new JunkJet();
        harness.setLibrary(player1, List.of(top));
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));

        harness.activateAbility(player1, junkIndex, null, null);
        assertThat(countPermanents(player1, "Junk")).isZero();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, top.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Junk Jet");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void junkCannotBeActivatedOutsideMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DogmeatEverLoyal());
        cast(List.of(target.getId()));
        harness.forceStep(TurnStep.UPKEEP);
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void junkCanBeSacrificedWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DogmeatEverLoyal());
        cast(List.of(target.getId()));
        harness.setLibrary(player1, List.of());
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));

        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void canDeclineTargetEvenWhenLegendaryPermanentExists() {
        harness.addToBattlefield(player2, new DogmeatEverLoyal());

        cast(List.of());

        harness.assertOnBattlefield(player2, "Dogmeat, Ever Loyal");
        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(countPermanents(player2, "Junk")).isZero();
    }

    @Test
    void tappedJunkCannotBeSacrificedToItsAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DogmeatEverLoyal());
        cast(List.of(target.getId()));
        Permanent junk = findPermanent(player1, "Junk");
        junk.tap();
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void junkPlayPermissionExpiresAfterTheTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DogmeatEverLoyal());
        cast(List.of(target.getId()));
        JunkJet top = new JunkJet();
        harness.setLibrary(player1, List.of(top));
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));
        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Junk Jet");
    }
    private void cast(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new CommanderSofiaDaguerre()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
