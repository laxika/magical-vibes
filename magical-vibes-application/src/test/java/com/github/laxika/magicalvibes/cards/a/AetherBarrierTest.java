package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Oraxid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherBarrier.class, Oraxid.class})
class AetherBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Aether Barrier triggers for an opponent's creature spell")
    void triggersForOpponentCreatureSpell() {
        harness.addToBattlefield(player1, new AetherBarrier());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Oraxid(), "{3}{U}");

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Aether Barrier also triggers when its controller casts a creature spell")
    void triggersWhenControllerCastsCreatureSpell() {
        harness.addToBattlefield(player1, new AetherBarrier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new Oraxid(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Aether Barrier does not trigger for a noncreature spell")
    void doesNotTriggerForNoncreatureSpell() {
        harness.addToBattlefield(player1, new AetherBarrier());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AetherBarrier(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The caster may pay {1} instead of sacrificing a permanent")
    void casterPaysInsteadOfSacrificing() {
        harness.addToBattlefield(player1, new AetherBarrier());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new Oraxid());
        harness.castFromHand(player2, new Oraxid(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Oraxid");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining to pay makes the caster sacrifice a permanent of their choice")
    void casterSacrificesWhenTheyDeclineToPay() {
        harness.addToBattlefield(player1, new AetherBarrier());
        harness.addToBattlefield(player2, new Oraxid());
        harness.addToBattlefield(player2, new Oraxid());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Oraxid(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).hasSize(2);

        harness.handleMultiplePermanentsChosen(player2, List.of(choice.validIds().getFirst()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Oraxid");
    }
}
