package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glamdring.class, GrizzlyBears.class, GiantGrowth.class, Shock.class,
        CounselOfTheSoratami.class, Forest.class})
class GlamdringTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets first strike and +1/+0 for each instant or sorcery in its controller's graveyard")
    void equippedCreatureGetsGraveyardScalingBoostAndFirstStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new Shock(), new GiantGrowth(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage offers an instant or sorcery from hand with mana value at most the damage dealt")
    void combatDamageOffersEligibleFreeCast() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock eligible = new Shock();
        CounselOfTheSoratami tooExpensive = new CounselOfTheSoratami();
        Forest land = new Forest();
        harness.setHand(player1, List.of(eligible, tooExpensive, land));

        resolveCombat();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Shock").doesNotContain("Counsel of the Soratami");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligible.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensive, land);
    }

    @Test
    @DisplayName("Glamdring does not trigger when the equipped creature deals combat damage only to a creature")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of(new Shock()));

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
