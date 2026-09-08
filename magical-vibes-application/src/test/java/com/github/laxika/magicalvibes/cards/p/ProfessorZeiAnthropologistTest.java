package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessorZeiAnthropologist.class, Divination.class, GrizzlyBears.class, LightningBolt.class})
class ProfessorZeiAnthropologistTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and draws a card")
    void discardsAndDraws() {
        Permanent professor = addReadyProfessor();
        Card discarded = new GrizzlyBears();
        Card drawn = new LightningBolt();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, professorIndex(professor), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(professor.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(drawn.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(discarded.getId());
    }

    @Test
    @DisplayName("Sacrifices itself and returns a target instant or sorcery")
    void sacrificesAndReturnsTargetSpell() {
        Permanent professor = addReadyProfessor();
        Card returned = new Divination();
        harness.setGraveyard(player1, List.of(returned));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, professorIndex(professor), 1,
                List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(professor);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(professor.getCard().getId())
                .doesNotContain(returned.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(returned.getId());
    }

    @Test
    @DisplayName("Rejects a creature as a graveyard target")
    void rejectsCreatureTarget() {
        Permanent professor = addReadyProfessor();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, professorIndex(professor), 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(professor);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(creature.getId());
    }

    @Test
    @DisplayName("The return ability can activate only during its controller's turn")
    void returnAbilityRequiresYourTurn() {
        Permanent professor = addReadyProfessor();
        Card returned = new LightningBolt();
        harness.setGraveyard(player1, List.of(returned));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, professorIndex(professor), 1, List.of(returned.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent addReadyProfessor() {
        Permanent professor = new Permanent(new ProfessorZeiAnthropologist());
        professor.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(professor);
        return professor;
    }

    private int professorIndex(Permanent professor) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(professor);
    }
}
