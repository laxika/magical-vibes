package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FainTheBroker.class, GrizzlyBears.class, Spellbook.class})
class FainTheBrokerTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndPutsTwoCountersOnTargetCreature() {
        Permanent fain = addReadyFain();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(fain.isTapped()).isTrue();
    }

    @Test
    void removesAnyCounterFromCreatureAndCreatesTreasure() {
        Permanent fain = addReadyFain();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(findPermanent(player1, "Treasure").getCard().isToken()).isTrue();
        assertThat(fain.isTapped()).isTrue();
    }

    @Test
    void sacrificesArtifactAndCreatesFlyingInkling() {
        Permanent fain = addReadyFain();
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(fain.isTapped()).isTrue();
    }

    @Test
    void paysManaToUntapFain() {
        Permanent fain = addReadyFain();
        fain.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(fain.isTapped()).isFalse();
    }

    @Test
    void canSacrificeFainAndStillResolveItsAbility() {
        addReadyFain();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Fain, the Broker");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canPutCountersOnFainBySacrificingAnotherCreature() {
        Permanent fain = addReadyFain();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, fain.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(fain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void removesExactlyOneCounterFromFainAsAnActivationCost() {
        Permanent fain = addReadyFain();
        fain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(fain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void mustLetControllerChooseWhichKindOfCounterToRemove() {
        Permanent fain = addReadyFain();
        fain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        fain.setCounterCount(CounterType.FLYING, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(fain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fain.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void cannotRemoveCountersFromOpponentsCreaturesOrControlledNoncreatures() {
        addReadyFain();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new Spellbook());
        Permanent artifact = findPermanent(player1, "Spellbook");
        artifact.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void canSacrificeItsTreasureForAnInklingAfterUntapping() {
        Permanent fain = addReadyFain();
        fain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 3, null, null);
        assertThat(fain.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(fain.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Inkling")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);
        assertThat(fain.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Inkling").getCard().isToken()).isTrue();
    }

    @Test
    void untapAbilityWorksWhileSummoningSickButDoesNotEnableTapAbilities() {
        Permanent fain = addReadyFain();
        fain.setSummoningSick(true);
        fain.tap();
        fain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(fain.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyFain() {
        return addCreatureReady(player1, new FainTheBroker());
    }
}
