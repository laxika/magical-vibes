package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.s.ScentOfJasmine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfUnbinding.class, AncientSilverback.class, ScentOfJasmine.class})
class CurseOfUnbindingTest extends BaseCardTest {

    @Test
    @DisplayName("At enchanted player's upkeep, steals the first revealed creature and mills the rest")
    void stealsFirstRevealedCreatureAndMillsTheRest() {
        placeCurseOnPlayer(player1, player2);
        Card nonCreatureBefore = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        Card nonCreatureAfter = new ScentOfJasmine();
        harness.setLibrary(player2, List.of(nonCreatureBefore, creature, nonCreatureAfter));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard())
                .doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(nonCreatureBefore);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonCreatureAfter);
    }

    @Test
    @DisplayName("If no creature is revealed, the enchanted player's entire library goes to their graveyard")
    void millsEntireLibraryWhenNoCreatureExists() {
        placeCurseOnPlayer(player1, player2);
        Card firstNonCreature = new ScentOfJasmine();
        Card secondNonCreature = new ScentOfJasmine();
        harness.setLibrary(player2, List.of(firstNonCreature, secondNonCreature));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstNonCreature, secondNonCreature);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .doesNotContain(firstNonCreature, secondNonCreature);
    }

    @Test
    @DisplayName("The trigger fires only during the enchanted player's upkeep")
    void triggerDoesNotFireDuringAnotherPlayersUpkeep() {
        placeCurseOnPlayer(player1, player2);
        Card nonCreature = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        harness.setLibrary(player2, List.of(nonCreature, creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonCreature, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = new Permanent(new CurseOfUnbinding());
        curse.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(curse);
    }
}
