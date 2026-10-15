package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnabaAncestor.class, AnabaBodyguard.class, Roterothopter.class})
class AnabaAncestorTest extends BaseCardTest {

    @Test
    @DisplayName("Another target Minotaur gets +1/+1 until end of turn")
    void boostsAnotherMinotaur() {
        setupAncestor();
        UUID targetId = harness.getPermanentId(player1, "Anaba Bodyguard");

        harness.activateAbility(player1, 0, null, targetId);
        assertThat(findPermanent(player1, "Anaba Ancestor").isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent minotaur = findPermanent(player1, "Anaba Bodyguard");
        assertThat(minotaur.getPowerModifier()).isEqualTo(1);
        assertThat(minotaur.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can boost a Minotaur an opponent controls")
    void boostsOpponentMinotaur() {
        setupAncestor();
        harness.addToBattlefield(player2, new AnabaBodyguard());
        UUID targetId = harness.getPermanentId(player2, "Anaba Bodyguard");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Anaba Bodyguard").getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupAncestor();
        UUID targetId = harness.getPermanentId(player1, "Anaba Bodyguard");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent minotaur = findPermanent(player1, "Anaba Bodyguard");
        assertThat(minotaur.getPowerModifier()).isEqualTo(0);
        assertThat(minotaur.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        setupAncestor();
        UUID selfId = harness.getPermanentId(player1, "Anaba Ancestor");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, selfId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Minotaur creature")
    void cannotTargetNonMinotaur() {
        setupAncestor();
        harness.addToBattlefield(player1, new Roterothopter());
        UUID thopterId = harness.getPermanentId(player1, "Roterothopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thopterId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        setupAncestor();
        findPermanent(player1, "Anaba Ancestor").setSummoningSick(true);
        UUID targetId = harness.getPermanentId(player1, "Anaba Bodyguard");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Anaba Ancestor").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        setupAncestor();
        findPermanent(player1, "Anaba Ancestor").tap();
        UUID targetId = harness.getPermanentId(player1, "Anaba Bodyguard");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Anaba Ancestor is a legal target")
    void boostsAnotherAncestor() {
        setupAncestor();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AnabaAncestor());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Anaba Ancestor").getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        setupAncestor();
        Permanent source = findPermanent(player1, "Anaba Ancestor");
        Permanent target = findPermanent(player1, "Anaba Bodyguard");
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void setupAncestor() {
        Permanent ancestor = harness.addToBattlefieldAndReturn(player1, new AnabaAncestor());
        harness.addToBattlefield(player1, new AnabaBodyguard());
        ancestor.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
