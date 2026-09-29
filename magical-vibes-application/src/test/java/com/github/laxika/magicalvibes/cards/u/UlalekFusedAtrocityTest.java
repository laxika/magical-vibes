package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EldraziDevastator;
import com.github.laxika.magicalvibes.cards.l.LeylineOfAnticipation;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlalekFusedAtrocity.class, EldraziDevastator.class,
        LeylineOfAnticipation.class, ProdigalPyromancer.class})
class UlalekFusedAtrocityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Eldrazi spell offers to pay {C}{C}")
    void eldrazispellOffersCopyPayment() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the payment does not copy the Eldrazi spell")
    void decliningPaymentDoesNotCopy() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Eldrazi Devastator");
    }

    @Test
    @DisplayName("Paying copies the current spell and activated ability")
    void payingCopiesCurrentSpellAndActivatedAbility() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent pyromancer = addReadyPyromancer(player1);
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        int pyromancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer);
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Eldrazi Devastator"));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Prodigal Pyromancer"));

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyPyromancer(Player player) {
        Permanent permanent = new Permanent(new ProdigalPyromancer());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
