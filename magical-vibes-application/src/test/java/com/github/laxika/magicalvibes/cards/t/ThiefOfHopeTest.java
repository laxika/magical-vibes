package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.s.SibilantSpirit;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThiefOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell drains 1 life from the targeted opponent")
    void arcaneCastDrainsOpponent() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int startingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Casting a Spirit spell drains 1 life from the targeted opponent")
    void spiritCastDrainsOpponent() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        harness.setHand(player1, List.of(new LanternKami()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int startingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int startingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Soulshift 2 returns a targeted Spirit with mana value 2 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        Card kami = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(kami)));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(kami.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kami.getId()));
    }

    @Test
    @DisplayName("Soulshift 2 cannot return a Spirit with mana value 3 or greater")
    void expensiveSpiritNotTargetable() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new SibilantSpirit())));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
