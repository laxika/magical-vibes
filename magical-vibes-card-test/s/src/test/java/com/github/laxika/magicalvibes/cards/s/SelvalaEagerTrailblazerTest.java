package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelvalaEagerTrailblazer.class, GrizzlyBears.class, LlanowarElves.class})
class SelvalaEagerTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("The mana ability adds one mana per distinct controlled creature power")
    void addsManaForDistinctCreaturePowers() {
        Permanent selvala = addCreatureReady(player1, new SelvalaEagerTrailblazer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(selvala.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature creates a Mercenary with its boost ability")
    void castingCreatureCreatesMercenary() {
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaEagerTrailblazer());
        Card bearsCard = new GrizzlyBears();
        harness.setHand(player1, List.of(bearsCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        mercenary.setSummoningSick(false);

        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(selvala.isTapped()).isFalse();
    }

    @Test
    void manaAbilityCountsSelvalaAndIgnoresOpposingCreaturesWithoutUsingStack() {
        addCreatureReady(player1, new SelvalaEagerTrailblazer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mercenaryIsCreatedBeforeCreatureSpellResolvesAndCannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SelvalaEagerTrailblazer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null,
                findPermanent(player1, "Selvala, Eager Trailblazer").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Mercenary").isTapped()).isFalse();
    }

    @Test
    void mercenaryBoostChangesDistinctPowerCountAndExpiresAtEndOfTurn() {
        addCreatureReady(player1, new SelvalaEagerTrailblazer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, index, 0, null, mercenary.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
        assertThat(mercenary.isTapped()).isTrue();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
    }

    @Test
    void castingSelvalaDoesNotTriggerHerOwnAbility() {
        harness.setHand(player1, List.of(new SelvalaEagerTrailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Selvala, Eager Trailblazer");
        assertThat(countPermanents(player1, "Mercenary")).isZero();
    }

    @Test
    void opponentsCreatureCastDoesNotCreateMercenary() {
        harness.addToBattlefield(player1, new SelvalaEagerTrailblazer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mercenary")).isZero();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void mercenaryCannotTargetOpponentOrActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new SelvalaEagerTrailblazer());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null,
                findPermanent(player1, "Llanowar Elves").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
    }
}
