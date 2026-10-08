package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfEmancipation.class, ChromeCat.class, Island.class})
class SoulOfEmancipationTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys up to three other nonland permanents and gives their controllers Angels")
    void destroysPermanentsAndCreatesAngelsForTheirControllers() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new ChromeCat());
        Permanent firstOpponentPermanent = harness.addToBattlefieldAndReturn(player2, new ChromeCat());
        Permanent secondOpponentPermanent = harness.addToBattlefieldAndReturn(player2, new ChromeCat());

        castSoul(List.of(ownPermanent.getId(), firstOpponentPermanent.getId(), secondOpponentPermanent.getId()));

        assertThat(findPermanents(player1, "Chrome Cat")).isEmpty();
        assertThat(findPermanents(player2, "Chrome Cat")).isEmpty();
        assertThat(findPermanents(player1, "Soul of Emancipation")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).hasSize(2);
        assertThat(findPermanents(player1, "Angel")).allSatisfy(this::assertAngel);
        assertThat(findPermanents(player2, "Angel")).allSatisfy(this::assertAngel);
    }

    @Test
    @DisplayName("The source and lands cannot be chosen")
    void sourceAndLandsAreNotLegalTargets() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new SoulOfEmancipation(), "{4}{G}{W}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Soul of Emancipation")).hasSize(1);
        assertThat(findPermanents(player1, "Island")).hasSize(1);
    }

    @Test
    void mayChooseZeroTargetsEvenWhenLegalTargetsExist() {
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SoulOfEmancipation());

        castSoul(List.of());

        assertThat(findPermanents(player2, "Soul of Emancipation")).containsExactly(other);
        assertThat(findPermanents(player1, "Angel")).isEmpty();
        assertThat(findPermanents(player2, "Angel")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shieldCounterPreventsDestructionButControllerStillCreatesAngel() {
        Permanent protectedPermanent = harness.addToBattlefieldAndReturn(player2, new SoulOfEmancipation());
        protectedPermanent.setCounterCount(CounterType.SHIELD, 1);
        Permanent unprotectedPermanent = harness.addToBattlefieldAndReturn(player1, new SoulOfEmancipation());

        castSoul(List.of(protectedPermanent.getId(), unprotectedPermanent.getId()));

        assertThat(findPermanents(player2, "Soul of Emancipation")).containsExactly(protectedPermanent);
        assertThat(protectedPermanent.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertInGraveyard(player1, "Soul of Emancipation");
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
    }

    @Test
    void regeneratedTargetStillGivesItsControllerAnAngel() {
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SoulOfEmancipation());
        other.setRegenerationShield(1);

        castSoul(List.of(other.getId()));

        assertThat(findPermanents(player2, "Soul of Emancipation")).containsExactly(other);
        assertThat(other.getRegenerationShield()).isZero();
        assertThat(other.isTapped()).isTrue();
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
    }

    @Test
    void destroyedTokenIsReplacedWithAnAngel() {
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SoulOfEmancipation());
        castSoul(List.of(other.getId()));
        Permanent firstAngel = findPermanents(player2, "Angel").getFirst();

        castSoul(List.of(firstAngel.getId()));

        assertThat(findPermanents(player2, "Angel")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).doesNotContain(firstAngel);
        assertThat(findPermanents(player2, "Angel")).allSatisfy(this::assertAngel);
    }

    @Test
    void targetThatLeavesBattlefieldDoesNotCreateAngelWhileOtherTargetResolves() {
        Permanent departing = harness.addToBattlefieldAndReturn(player1, new SoulOfEmancipation());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new SoulOfEmancipation());
        harness.setHand(player1, List.of(new SoulOfEmancipation()));
        addManaForSoul();
        harness.castCreature(player1, 0, List.of(departing.getId(), remaining.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, departing));

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Soul of Emancipation");
        assertThat(findPermanents(player1, "Angel")).isEmpty();
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
    }

    private void castSoul(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SoulOfEmancipation()));
        addManaForSoul();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addManaForSoul() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void assertAngel(Permanent angel) {
        assertThat(angel.getCard().getPower()).isEqualTo(3);
        assertThat(angel.getCard().getToughness()).isEqualTo(3);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(angel.getCard().isToken()).isTrue();
    }
}
