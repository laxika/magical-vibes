package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.c.ChimericIdol;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.m.ManaVapors;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicTheft.class, ManaVapors.class, DivingGriffin.class, WintermoonMesa.class,
        Abolish.class, ChimericIdol.class})
class PsychicTheftTest extends BaseCardTest {

    @Test
    void choosesOnlyAnInstantOrSorceryFromTheRevealedHand() {
        Card land = new WintermoonMesa();
        Card creature = new DivingGriffin();
        Card spell = new ManaVapors();
        harness.setHand(player2, List.of(land, creature, spell));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .hasMessageContaining("valid");
        harness.handleCardChosen(player1, 2);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
        assertThat(gd.exilePlayPermissions.get(spell.getId())).isEqualTo(player1.getId());
    }

    @Test
    void doesNothingWhenTheTargetHasNoInstantOrSorcery() {
        Card land = new WintermoonMesa();
        Card creature = new DivingGriffin();
        harness.setHand(player2, List.of(land, creature));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
    }

    @Test
    void returnsTheUncastCardToItsOwnersHandAtTheNextEndStep() {
        Card spell = new ManaVapors();
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(spell);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(spell);
    }

    @Test
    void mayCastTheExiledCardBeforeTheDelayedReturn() {
        Card spell = new ManaVapors();
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
    }

    @Test
    void canCastAnExiledInstantInResponseToItsDelayedReturn() {
        Card instant = new Abolish();
        Card theft = new PsychicTheft();
        var artifact = harness.addToBattlefieldAndReturn(player2, new ChimericIdol());
        harness.setHand(player2, List.of(instant));
        harness.setHand(player1, List.of(theft));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castFromExile(player1, instant.getId(), artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(instant);
        harness.assertNotOnBattlefield(player2, "Chimeric Idol");
    }
}
