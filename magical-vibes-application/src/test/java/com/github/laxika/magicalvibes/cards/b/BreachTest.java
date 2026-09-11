package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
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

@CardUsed({Breach.class, Cathodion.class, CoralMerfolk.class, GorillaWarrior.class,
        BloodVassal.class, WornPowerstone.class})
class BreachTest extends BaseCardTest {

    @Test
    @DisplayName("Breach gives target creature +2/+0 and fear")
    void resolvesAllEffects() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        harness.setHand(player1, List.of(new Breach()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, gorilla.getId());
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, gorilla, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Breach can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.setHand(player1, List.of(new Breach()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, gorilla.getId());
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gorilla, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Breach's effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        harness.setHand(player1, List.of(new Breach()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, gorilla.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isZero();
        assertThat(gorilla.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, gorilla, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Breach cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GorillaWarrior());
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new Breach()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, powerstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Breach-granted fear prevents a nonblack, nonartifact creature from blocking")
    void fearPreventsNonBlackNonArtifactBlocker() {
        Permanent attacker = castBreachOn(new GorillaWarrior());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());

        declareAttackers(List.of(indexOf(player1, attacker)));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Breach-granted fear allows black and artifact creature blockers")
    void fearAllowsBlackAndArtifactCreatureBlockers() {
        Permanent attacker = castBreachOn(new GorillaWarrior());
        Permanent blackBlocker = addCreatureReady(player2, new BloodVassal());
        Permanent artifactBlocker = addCreatureReady(player2, new Cathodion());

        declareAttackers(List.of(indexOf(player1, attacker)));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(indexOf(player2, blackBlocker), indexOf(player1, attacker)),
                new BlockerAssignment(indexOf(player2, artifactBlocker), indexOf(player1, attacker))));

        assertThat(blackBlocker.isBlocking()).isTrue();
        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    private Permanent castBreachOn(Card targetCard) {
        Permanent target = addCreatureReady(player1, targetCard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Breach()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        return target;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
