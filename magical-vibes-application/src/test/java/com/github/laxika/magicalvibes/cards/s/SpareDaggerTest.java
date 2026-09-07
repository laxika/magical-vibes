package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpareDagger.class, GrizzlyBears.class})
class SpareDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachDagger(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature offers the sacrifice and deals 1 damage")
    void acceptingSacrificeDealsDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachDagger(player1, bears);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MayAbilityTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        resolveCombat();

        harness.assertInGraveyard(player1, "Spare Dagger");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declining the sacrifice keeps Spare Dagger attached")
    void decliningKeepsDaggerAttached() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = attachDagger(player1, bears);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveCombat();

        harness.assertOnBattlefield(player1, "Spare Dagger");
        assertThat(dagger.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Only the equipped creature gets the attack trigger")
    void otherCreatureAttackingDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        attachDagger(player1, equipped);

        declareAttackers(player1, List.of(1));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Spare Dagger");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent attachDagger(Player player, Permanent host) {
        Permanent dagger = new Permanent(new SpareDagger());
        dagger.setSummoningSick(false);
        dagger.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(dagger);
        return dagger;
    }
}
