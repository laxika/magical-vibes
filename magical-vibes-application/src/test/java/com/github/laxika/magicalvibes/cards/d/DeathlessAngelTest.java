package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathlessAngel.class, GlorySeeker.class, Plains.class, FlameSlash.class})
class DeathlessAngelTest extends BaseCardTest {

    @Test
    void activatedAbilityGrantsTargetCreatureIndestructibleUntilEndOfTurn() {
        addReadyAngel(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        target.setSummoningSick(false);

        activateAbility(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addReadyAngel(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAngel(Player player) {
        Permanent angel = harness.addToBattlefieldAndReturn(player, new DeathlessAngel());
        angel.setSummoningSick(false);
        return angel;
    }

    @Test
    void canTargetItselfWhileSummoningSickAndTapped() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new DeathlessAngel());
        angel.setSummoningSick(true);
        angel.setTapped(true);

        activateAbility(angel.getId());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(angel.isTapped()).isTrue();
    }

    @Test
    void indestructibleCreatureSurvivesLethalDamage() {
        addReadyAngel(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        activateAbility(target.getId());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player1, "Glory Seeker");
    }

    @Test
    void cannotActivateWithOnlyOneWhiteMana() {
        Permanent angel = addReadyAngel(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void activateAbility(java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }
}
