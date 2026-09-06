package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphMikeyTroublemakers.class, FountainOfYouth.class, GrizzlyBears.class, ChandraNalaar.class})
class RaphMikeyTroublemakersTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals a creature, offers all legal attack destinations, and puts it tapped and attacking")
    void revealsCreatureAndChoosesAttackDestination() {
        addCreatureReady(player1, new RaphMikeyTroublemakers());
        Permanent planeswalker = addTestPlaneswalker(player2, 4);
        Permanent battle = addTestBattle(player1, player2);
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        assertThat(choice.validPermanentIds()).contains(planeswalker.getId(), battle.getId());

        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getAttackTarget()).isEqualTo(battle.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Fountain of Youth");
    }

    private Permanent addTestPlaneswalker(Player player, int loyalty) {
        ChandraNalaar card = new ChandraNalaar();
        card.setLoyalty(loyalty);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private Permanent addTestBattle(Player controller, Player protector) {
        Card card = new Card();
        card.setName("Test Battle");
        card.setType(CardType.BATTLE);
        Permanent battle = new Permanent(card);
        battle.setProtectorPlayerId(protector.getId());
        gd.playerBattlefields.get(controller.getId()).add(battle);
        return battle;
    }
}
