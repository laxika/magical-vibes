package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GokaTheUnjust;
import com.github.laxika.magicalvibes.cards.k.KashiTribeWarriors;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InitiateOfBlood.class, GokaTheUnjust.class, DevotedRetainer.class, KashiTribeWarriors.class,
        RendFlesh.class, Forest.class})
class InitiateOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a damaged creature and flips when that creature dies")
    void killsDamagedCreatureAndFlips() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new DevotedRetainer());

        UUID targetId = harness.getPermanentId(player2, "Devoted Retainer");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Devoted Retainer");
        assertThat(initiate.isTransformed()).isTrue();
        assertThat(initiate.getCard().getName()).isEqualTo("Goka the Unjust");
    }

    @Test
    @DisplayName("Does not flip while the damaged creature survives")
    void doesNotFlipWhenTargetSurvives() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Kashi-Tribe Warriors");
        assertThat(initiate.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips when something else finishes off the damaged creature later in the turn")
    void flipsWhenAnotherSourceKillsTheTarget() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveAllTriggers();
        assertThat(initiate.isTransformed()).isFalse();

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Kashi-Tribe Warriors");
        assertThat(initiate.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even when it was dealt damage this turn")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new Forest());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate the tap ability again before the Initiate untaps")
    void tapCostPreventsSecondActivation() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(initiate.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        resolveAllTriggers();
    }

    @Test
    @DisplayName("Does not flip when the targeted creature dies on a later turn")
    void doesNotFlipWhenTargetDiesNextTurn() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveAllTriggers();
        assertThat(initiate.isTransformed()).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Kashi-Tribe Warriors");
        assertThat(initiate.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Once flipped, Goka deals 4 damage to a damaged creature")
    void flippedGokaDeals4Damage() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        initiate.setTransformed(true);
        initiate.setCard(initiate.getOriginalCard().getBackFaceCard());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Kashi-Tribe Warriors");
    }

    @Test
    @DisplayName("Once flipped, Goka cannot target a creature that was not dealt damage this turn")
    void flippedGokaCannotTargetUndamagedCreature() {
        Permanent initiate = addCreatureReady(player1, new InitiateOfBlood());
        initiate.setTransformed(true);
        initiate.setCard(initiate.getOriginalCard().getBackFaceCard());
        harness.addToBattlefield(player2, new KashiTribeWarriors());

        UUID targetId = harness.getPermanentId(player2, "Kashi-Tribe Warriors");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }
}
