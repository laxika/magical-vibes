package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AfiyaGrove;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({TheMasterOfKeys.class, AfiyaGrove.class, Forest.class, GrizzlyBears.class, Pacifism.class})
class TheMasterOfKeysTest extends BaseCardTest {

    @Test
    void putsXCountersOnItAndMillsTwiceX() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TheMasterOfKeys()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, 2);
        resolveAllTriggers();

        Permanent master = findPermanent(player1, "The Master of Keys");
        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void givesEscapeToEnchantmentCardsInGraveyard() {
        harness.addToBattlefield(player1, new TheMasterOfKeys());
        harness.setGraveyard(player1, List.of(
                new AfiyaGrove(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Afiya Grove");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    void doesNotGiveEscapeToNonenchantmentCards() {
        harness.addToBattlefield(player1, new TheMasterOfKeys());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXDoesNotAddCountersOrMill() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TheMasterOfKeys()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "The Master of Keys")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void escapeRequiresThreeOtherCards() {
        harness.addToBattlefield(player1, new TheMasterOfKeys());
        harness.setGraveyard(player1, List.of(new AfiyaGrove(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void doesNotGrantEscapeToOpponentsEnchantments() {
        harness.addToBattlefield(player1, new TheMasterOfKeys());
        harness.setGraveyard(player2, List.of(
                new AfiyaGrove(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapedAuraReturnsToGraveyardWhenItsTargetBecomesIllegal() {
        harness.addToBattlefield(player1, new TheMasterOfKeys());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Pacifism(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(0, 1, 2));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }
}
