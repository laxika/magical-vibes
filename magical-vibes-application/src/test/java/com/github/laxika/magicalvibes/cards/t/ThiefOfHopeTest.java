package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.k.KamiOfAncientLaw;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThiefOfHope.class, DampenThought.class, KamiOfAncientLaw.class, LanternKami.class,
        MossKami.class, RendSpirit.class, SakuraTribeElder.class})
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
    @DisplayName("A Spirit spell cast by an opponent does not trigger Thief of Hope")
    void opponentSpiritCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        harness.setHand(player2, List.of(new LanternKami()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int playerOneStartingLife = gd.getLife(player1.getId());
        int playerTwoStartingLife = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(playerOneStartingLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(playerTwoStartingLife);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThiefOfHope());
        harness.setHand(player1, List.of(new SakuraTribeElder()));
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
        ThiefOfHope thief = new ThiefOfHope();
        harness.addToBattlefield(player1, thief);
        Card kami = new KamiOfAncientLaw();
        Card nonSpirit = new SakuraTribeElder();
        harness.setGraveyard(player1, new ArrayList<>(List.of(kami, nonSpirit)));

        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Thief of Hope"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(kami.getId());

        harness.handleMultipleCardsChosen(player1, List.of(kami.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kami.getId()));
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        ThiefOfHope thief = new ThiefOfHope();
        harness.addToBattlefield(player1, thief);
        Card kami = new KamiOfAncientLaw();
        harness.setGraveyard(player1, new ArrayList<>(List.of(kami)));

        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Thief of Hope"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(kami.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kami.getId()));
    }

    @Test
    @DisplayName("Soulshift 2 cannot return a Spirit with mana value 3 or greater")
    void expensiveSpiritNotTargetable() {
        ThiefOfHope thief = new ThiefOfHope();
        harness.addToBattlefield(player1, thief);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new MossKami())));

        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Thief of Hope"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
