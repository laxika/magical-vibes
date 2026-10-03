package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.s.ScentOfJasmine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    @DisplayName("The Aura can be cast enchanting an opponent")
    void castsEnchantingOpponent() {
        harness.setHand(player1, List.of(new CurseOfUnbinding()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getAttachedTo())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A creature on top enters without milling cards or revealing the next creature")
    void stopsAtTopCreature() {
        placeCurseOnPlayer(player1, player2);
        Card firstCreature = new AncientSilverback();
        Card secondCreature = new AncientSilverback();
        harness.setLibrary(player2, List.of(firstCreature, secondCreature));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(firstCreature).doesNotContain(secondCreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchanting yourself puts the revealed creature under your control")
    void canEnchantController() {
        placeCurseOnPlayer(player1, player1);
        Card creature = new AncientSilverback();
        Card remaining = new ScentOfJasmine();
        harness.setLibrary(player1, List.of(creature, remaining));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library reveals nothing and does not make the enchanted player lose")
    void emptyLibraryDoesNothing() {
        placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The upkeep ability still resolves after the Curse leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        placeCurseOnPlayer(player1, player2);
        Card nonCreature = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        Card remaining = new ScentOfJasmine();
        harness.setLibrary(player2, List.of(nonCreature, creature, remaining));

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonCreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
    }

    private void placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = harness.addToBattlefieldAndReturn(controller, new CurseOfUnbinding());
        curse.setAttachedTo(enchantedPlayer.getId());
    }
}
