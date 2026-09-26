package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ClergyOfTheHolyNimbus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlyphOfReincarnation.class, WallOfGlare.class, WallOfWood.class, GrizzlyBears.class,
        LlanowarElves.class, ClergyOfTheHolyNimbus.class, Forest.class})
class GlyphOfReincarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures blocked by the targeted Wall and returns one creature per death")
    void destroysBlockedCreaturesAndReturnsOneCreaturePerDeath() {
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        Permanent affectedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAffectedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unaffectedAttacker = addCreatureReady(player1, new GrizzlyBears());
        affectedAttacker.setAttacking(true);
        secondAffectedAttacker.setAttacking(true);
        unaffectedAttacker.setAttacking(true);

        Card returnedCreature = new LlanowarElves();
        returnedCreature.setOwnerId(player1.getId());
        Card secondReturnedCreature = new LlanowarElves();
        secondReturnedCreature.setOwnerId(player1.getId());
        harness.setGraveyard(player1, List.of(returnedCreature, secondReturnedCreature));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GlyphOfReincarnation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, wall.getId());

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).contains(returnedCreature);

        harness.handleGraveyardCardChosen(player2, choice.cardPool().indexOf(returnedCreature));
        PendingInteraction.GraveyardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(secondChoice).isNotNull();
        harness.handleGraveyardCardChosen(player2, secondChoice.cardPool().indexOf(secondReturnedCreature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(unaffectedAttacker, matchingPermanent(player1, returnedCreature),
                        matchingPermanent(player1, secondReturnedCreature))
                .doesNotContain(affectedAttacker, secondAffectedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(wall);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(affectedAttacker.getCard(), secondAffectedAttacker.getCard());
    }

    @Test
    @DisplayName("Cannot be cast before combat has ended")
    void cannotCastBeforeCombatEnds() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        harness.setHand(player2, List.of(new GlyphOfReincarnation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWallCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GlyphOfReincarnation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the controller from the last block and returns only a creature under its owner's control")
    void usesHistoricalBlockControllerAndOwnerControl() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Card attackerCard = new GrizzlyBears();
        attackerCard.setOwnerId(player1.getId());
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        Card nonCreature = new Forest();
        Card returnedCreature = new LlanowarElves();
        returnedCreature.setOwnerId(player2.getId());
        Card otherPlayerCreature = new LlanowarElves();
        otherPlayerCreature.setOwnerId(player1.getId());
        harness.setGraveyard(player1, List.of(nonCreature, returnedCreature));
        harness.setGraveyard(player2, List.of(otherPlayerCreature));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GlyphOfReincarnation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, wall.getId());

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).contains(returnedCreature)
                .doesNotContain(nonCreature, otherPlayerCreature);

        harness.handleGraveyardCardChosen(player2, choice.cardPool().indexOf(returnedCreature));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(wall, matchingPermanent(player2, returnedCreature));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(nonCreature, attackerCard)
                .doesNotContain(returnedCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(otherPlayerCreature);
    }

    @Test
    @DisplayName("Destroys a creature with a regeneration ability because it cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent attacker = addCreatureReady(player1, new ClergyOfTheHolyNimbus());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GlyphOfReincarnation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, wall.getId());

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).contains(attacker.getCard());
        harness.handleGraveyardCardChosen(player2, choice.cardPool().indexOf(attacker.getCard()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(matchingPermanent(player1, attacker.getCard()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(attacker.getCard());
    }

    private Permanent matchingPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
