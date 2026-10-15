package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlleyStrangler;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RenegadeMap;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeldfastEngineer.class, Ornithopter.class, AlleyStrangler.class, RenegadeMap.class})
class WeldfastEngineerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Boosts a target artifact creature you control by +2/+0")
    void boostsTargetArtifactCreature() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, familiar.getId());
        harness.passBothPriorities();

        assertThat(familiar.getPowerModifier()).isEqualTo(2);
        assertThat(familiar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target an artifact creature controlled by an opponent")
    void cannotTargetOpponentArtifactCreature() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent familiar = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, familiar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlleyStrangler());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, familiar.getId());
        harness.passBothPriorities();
        assertThat(familiar.getPowerModifier()).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(familiar.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(thopter.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Does not ask for a target when no artifact creature is controlled")
    void noLegalTargets() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        harness.addToBattlefield(player1, new RenegadeMap());
        harness.addToBattlefield(player2, new Ornithopter());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent map = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, map.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Only the chosen artifact creature gets the boost")
    void onlyChosenCreatureIsBoosted() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(chosen.getPowerModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The trigger still resolves after the Engineer leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new WeldfastEngineer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        gd.playerBattlefields.get(player1.getId()).remove(engineer);
        gd.playerGraveyards.get(player1.getId()).add(engineer.getCard());
        harness.passBothPriorities();

        assertThat(thopter.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost does not resolve if the target changes controllers")
    void targetChangingControllersIsIllegal() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        gd.playerBattlefields.get(player1.getId()).remove(thopter);
        gd.playerBattlefields.get(player2.getId()).add(thopter);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(thopter.getPowerModifier()).isZero();
    }
}
