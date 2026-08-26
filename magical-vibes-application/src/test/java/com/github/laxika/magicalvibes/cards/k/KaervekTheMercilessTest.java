package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaervekTheMerciless.class, Fireball.class, GrizzlyBears.class, SuntailHawk.class})
class KaervekTheMercilessTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell triggers damage to a player equal to its mana value")
    void opponentSpellDealsManaValueDamageToPlayer() {
        harness.addToBattlefield(player1, new KaervekTheMerciless());
        setUpOpponentSpell(new GrizzlyBears(), ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's spell can target a creature")
    void opponentSpellDealsManaValueDamageToCreature() {
        harness.addToBattlefield(player1, new KaervekTheMerciless());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");
        setUpOpponentSpell(new GrizzlyBears(), ManaColor.GREEN, 2);

        harness.handlePermanentChosen(player1, hawkId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("X spell uses its mana value including X")
    void xSpellUsesManaValueIncludingX() {
        harness.addToBattlefield(player1, new KaervekTheMerciless());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 4);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player2, 0, 3, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Controller's own spell does not trigger Kaervek")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KaervekTheMerciless());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void setUpOpponentSpell(Card spell, ManaColor manaColor, int mana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, manaColor, mana);
        harness.castCreature(player2, 0);
    }
}
