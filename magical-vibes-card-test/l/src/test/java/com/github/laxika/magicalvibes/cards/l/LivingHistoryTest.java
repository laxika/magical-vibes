package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingHistory.class, Disentomb.class, GrizzlyBears.class})
class LivingHistoryTest extends BaseCardTest {

    @Test
    void createsRedAndWhiteSpiritWhenItEnters() {
        harness.setHand(player1, List.of(new LivingHistory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getEffectivePower()).isEqualTo(2);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
        assertThat(spirit.getEffectiveColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }

    @Test
    void boostsTargetAttackerAfterCardLeavesGraveyard() {
        harness.addToBattlefield(player1, new LivingHistory());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, graveyardCard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenNoCardLeftGraveyardThisTurn() {
        harness.addToBattlefield(player1, new LivingHistory());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void triggersOnceForMultipleAttackersAndOnlyBoostsTheChosenAttacker() {
        harness.addToBattlefield(player1, new LivingHistory());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, graveyardCard.getId());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
    }

}
