package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CapashenKnight;
import com.github.laxika.magicalvibes.cards.p.PatternOfRebirth;
import com.github.laxika.magicalvibes.cards.s.Sanctimony;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.PendingInteraction;

@CardUsed({Replenish.class, Sanctimony.class, CapashenKnight.class, PatternOfRebirth.class})
class ReplenishTest extends BaseCardTest {

    @Test
    void waitsForAuraAttachmentBeforeReturningNonAuraEnchantments() {
        var first = harness.addToBattlefieldAndReturn(player1, new CapashenKnight());
        var second = harness.addToBattlefieldAndReturn(player2, new CapashenKnight());
        Card aura = new PatternOfRebirth();
        Card enchantment = new Sanctimony();
        harness.setGraveyard(player1, List.of(aura, enchantment));
        castReplenish();

        var choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.assertNotOnBattlefield(player1, "Sanctimony");
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(findPermanent(player1, "Pattern of Rebirth").getAttachedTo()).isEqualTo(second.getId());
        harness.assertOnBattlefield(player1, "Sanctimony");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura, enchantment);
    }

    private void castReplenish() {
        harness.castFromHand(player1, new Replenish(), "{3}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns all non-Aura enchantment cards from your graveyard to the battlefield")
    void returnsAllEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card enchantment = new Sanctimony();
        Card creature = new CapashenKnight();
        harness.setGraveyard(player1, List.of(enchantment, creature));

        castReplenish();

        harness.assertOnBattlefield(player1, "Sanctimony");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Leaves Auras with nothing to enchant in the graveyard")
    void leavesOrphanedAurasInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card aura = new PatternOfRebirth();
        harness.setGraveyard(player1, List.of(aura));

        castReplenish();

        harness.assertNotOnBattlefield(player1, "Pattern of Rebirth");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Returns an Aura attached to an opponent's legal creature")
    void returnsAuraAttachedToOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CapashenKnight());
        Card aura = new PatternOfRebirth();
        harness.setGraveyard(player1, List.of(aura));

        castReplenish();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(aura.getId())
                        && opponentCreature.getId().equals(permanent.getAttachedTo()));
        harness.assertNotInGraveyard(player1, "Pattern of Rebirth");
    }

    @Test
    @DisplayName("Does not return enchantments from an opponent's graveyard")
    void doesNotReturnOpponentsEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentEnchantment = new Sanctimony();
        harness.setGraveyard(player2, List.of(opponentEnchantment));

        castReplenish();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentEnchantment);
        harness.assertNotOnBattlefield(player1, "Sanctimony");
    }
}
