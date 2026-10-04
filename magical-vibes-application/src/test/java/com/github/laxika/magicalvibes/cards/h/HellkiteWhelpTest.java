package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteWhelp.class, VernadiShieldmate.class, ChildOfNight.class})
class HellkiteWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to a creature controlled by the defending player")
    void attackTriggerDealsDamage() {
        addCreatureReady(player1, new HellkiteWhelp());
        Permanent victim = addCreatureReady(player2, new VernadiShieldmate());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only creatures controlled by the defending player are legal targets")
    void targetsOnlyDefendingCreatures() {
        addCreatureReady(player1, new HellkiteWhelp());
        Permanent ownCreature = addCreatureReady(player1, new ChildOfNight());
        Permanent defendingCreature = addCreatureReady(player2, new ChildOfNight());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("No target selection occurs when the defending player controls no creatures")
    void noLegalTargetSkipsTrigger() {
        addCreatureReady(player1, new HellkiteWhelp());

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Attack damage kills a defending creature with one toughness")
    void attackTriggerDealsLethalDamage() {
        addCreatureReady(player1, new HellkiteWhelp());
        Permanent victim = addCreatureReady(player2, new ChildOfNight());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Child of Night");
        harness.assertInGraveyard(player2, "Child of Night");
    }

    @Test
    @DisplayName("Attack trigger still deals damage after its source leaves the battlefield")
    void attackTriggerResolvesWithoutSource() {
        Permanent whelp = addCreatureReady(player1, new HellkiteWhelp());
        Permanent victim = addCreatureReady(player2, new VernadiShieldmate());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        gd.playerBattlefields.get(player1.getId()).remove(whelp);
        gd.playerGraveyards.get(player1.getId()).add(whelp.getCard());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Hellkite Whelp that does not attack does not trigger")
    void nonattackingWhelpDoesNotTrigger() {
        addCreatureReady(player1, new HellkiteWhelp());
        addCreatureReady(player1, new VernadiShieldmate());
        Permanent victim = addCreatureReady(player2, new ChildOfNight());

        declareAttackers(List.of(1));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(victim.getMarkedDamage()).isZero();
    }
}
