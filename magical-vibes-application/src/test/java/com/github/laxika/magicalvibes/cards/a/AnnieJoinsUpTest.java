package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnnieJoinsUp.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        MentorOfTheMeek.class, TatyovaBenthicDruid.class})
class AnnieJoinsUpTest extends BaseCardTest {

    @Test
    @DisplayName("When Annie Joins Up enters, it deals 5 damage to an opponent's creature")
    void etbDealsFiveDamageToOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        castAnnie(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Annie Joins Up doubles a legendary creature's triggered ability")
    void doublesLegendaryCreatureTriggeredAbility() {
        harness.addToBattlefield(player1, new AnnieJoinsUp());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Annie Joins Up does not double a nonlegendary creature's triggered ability")
    void doesNotDoubleNonlegendaryCreatureTriggeredAbility() {
        harness.addToBattlefield(player1, new AnnieJoinsUp());
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Annie Joins Up cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1,
                new GrizzlyBears());

        harness.setHand(player1, List.of(new AnnieJoinsUp()));
        addAnnieMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAnnie(UUID targetId) {
        harness.setHand(player1, List.of(new AnnieJoinsUp()));
        addAnnieMana();
        harness.castEnchantment(player1, 0, targetId);
    }

    private void addAnnieMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
