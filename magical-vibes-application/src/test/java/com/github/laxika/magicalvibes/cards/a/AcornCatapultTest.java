package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.w.WallOfOmens;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcornCatapult.class, WallOfOmens.class, SakuraTribeElder.class, GarrukWildspeaker.class})
class AcornCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Target player takes 1 damage and creates a Squirrel")
    void targetPlayerCreatesSquirrel() {
        addReadyCatapult(player1);
        harness.setLife(player2, 20);
        addMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findSquirrels(player1)).isEmpty();
        assertThat(findSquirrels(player2)).hasSize(1);
    }

    @Test
    @DisplayName("Target permanent takes 1 damage and its controller creates a Squirrel")
    void targetPermanentControllerCreatesSquirrel() {
        addReadyCatapult(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new WallOfOmens());
        addMana(player1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(findSquirrels(player1)).isEmpty();
        assertThat(findSquirrels(player2)).hasSize(1);
    }

    @Test
    @DisplayName("A newly entered noncreature Catapult can activate and pays mana and tap costs")
    void newlyEnteredCatapultCanTargetItsController() {
        Permanent catapult = harness.addToBattlefieldAndReturn(player1, new AcornCatapult());
        harness.setLife(player1, 20);
        addMana(player1);

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(catapult.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findSquirrels(player2)).isEmpty();
        assertThat(findSquirrels(player1)).hasSize(1);
        Permanent squirrel = findSquirrels(player1).getFirst();
        assertThat(squirrel.getCard().isToken()).isTrue();
        assertThat(squirrel.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
        assertThat(squirrel.getCard().getPower()).isEqualTo(1);
        assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
        assertThat(squirrel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Lethal damage still creates a Squirrel for the creature's controller")
    void lethalDamageCreatesSquirrel() {
        addReadyCatapult(player1);
        Permanent elder = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());
        addMana(player1);

        harness.activateAbility(player1, 0, null, elder.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sakura-Tribe Elder");
        harness.assertInGraveyard(player2, "Sakura-Tribe Elder");
        assertThat(findSquirrels(player2)).hasSize(1);
        assertThat(findSquirrels(player1)).isEmpty();
    }

    @Test
    @DisplayName("Preventing the damage does not prevent token creation")
    void preventedDamageStillCreatesSquirrel() {
        addReadyCatapult(player1);
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfOmens());
        gd.preventAllDamageToAllCreatures = true;
        addMana(player1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isZero();
        assertThat(findSquirrels(player2)).hasSize(1);
        assertThat(findSquirrels(player1)).isEmpty();
    }

    @Test
    @DisplayName("An ability with a departed target creates no Squirrel")
    void departedTargetCreatesNoSquirrel() {
        addReadyCatapult(player1);
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfOmens());
        addMana(player1);
        harness.activateAbility(player1, 0, null, wall.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));
        harness.passBothPriorities();

        assertThat(findSquirrels(player1)).isEmpty();
        assertThat(findSquirrels(player2)).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An activated ability resolves after the Catapult leaves")
    void departedSourceStillDealsDamageAndCreatesSquirrel() {
        Permanent catapult = addReadyCatapult(player1);
        harness.setLife(player2, 20);
        addMana(player1);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, catapult));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findSquirrels(player2)).hasSize(1);
        assertThat(findSquirrels(player1)).isEmpty();
    }

    @Test
    @DisplayName("A targeted planeswalker loses loyalty and its controller creates the Squirrel")
    void planeswalkerControllerCreatesSquirrel() {
        addReadyCatapult(player1);
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 3);
        addMana(player1);

        harness.activateAbility(player1, 0, null, garruk.getId());
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(findSquirrels(player2)).hasSize(1);
        assertThat(findSquirrels(player1)).isEmpty();
    }

    @Test
    @DisplayName("Activation requires one mana")
    void cannotActivateWithoutMana() {
        Permanent catapult = addReadyCatapult(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(catapult.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Catapult cannot activate")
    void cannotActivateWhileTapped() {
        Permanent catapult = addReadyCatapult(player1);
        catapult.setTapped(true);
        addMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature artifact is not a legal any-target choice")
    void cannotTargetNoncreatureArtifact() {
        addReadyCatapult(player1);
        Permanent otherCatapult = harness.addToBattlefieldAndReturn(player2, new AcornCatapult());
        addMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCatapult.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCatapult(Player player) {
        return addCreatureReady(player, new AcornCatapult());
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private List<Permanent> findSquirrels(Player player) {
        return findPermanents(player, "Squirrel");
    }
}
