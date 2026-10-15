package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FootElite.class, GrizzlyBears.class, Swamp.class})
class FootEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts another creature you control and grants indestructible")
    void attackingBoostsAndProtectsAnotherCreature() {
        Permanent footElite = addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, footElite, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target Foot Elite itself")
    void cannotTargetItself() {
        Permanent footElite = addCreatureReady(player1, new FootElite());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, footElite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The attack trigger cannot target an opponent's creature or a land")
    void cannotTargetOpposingCreaturesOrNoncreatures() {
        addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new FootElite());
        Permanent opponent = addCreatureReady(player2, new FootElite());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking alone does not grant the ability to its source")
    void attackingWithNoLegalTargetDoesNothing() {
        Permanent footElite = addCreatureReady(player1, new FootElite());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, footElite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, footElite, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent footElite = addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new FootElite());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(footElite);
        gd.playerGraveyards.get(player1.getId()).add(footElite.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The target must still be controlled by the trigger's controller at resolution")
    void targetChangingControllerMakesTriggerFail() {
        addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new FootElite());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerBattlefields.get(player2.getId()).add(ally);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A removed target does not redirect the boost or protection to another creature")
    void removedTargetMakesTriggerFail() {
        Permanent footElite = addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new FootElite());
        Permanent otherAlly = addCreatureReady(player1, new FootElite());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerGraveyards.get(player1.getId()).add(ally.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, footElite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherAlly)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, footElite, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherAlly, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible protects against lethal damage and lasts through the end step")
    void protectionPreventsLethalDamageAndExpiresDuringCleanup() {
        addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new FootElite());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        ally.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ally);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ally);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
