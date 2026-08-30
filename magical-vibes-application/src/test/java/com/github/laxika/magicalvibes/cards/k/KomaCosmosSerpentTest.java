package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KomaCosmosSerpentTest extends BaseCardTest {

    @Test
    void createsKomasCoilOnEachUpkeep() {
        addReadyKoma();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Koma's Coil")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(3);
                    assertThat(token.getCard().getToughness()).isEqualTo(3);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SERPENT);
                });
    }

    @Test
    void cannotBeCounteredByCancel() {
        KomaCosmosSerpent koma = new KomaCosmosSerpent();
        harness.setHand(player1, List.of(koma));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, koma.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Koma, Cosmos Serpent");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void sacrificingAnotherSerpentTapsAndLocksTargetPermanent() {
        addReadyKoma();
        harness.addToBattlefieldAndReturn(player1, serpent());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Serpent"));

        elves.untap();
        int elvesIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elves);
        assertThatThrownBy(() -> harness.tapPermanent(player1, elvesIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.expireEndOfTurnFloatingEffects();
        assertThatCode(() -> harness.tapPermanent(player1, elvesIndex)).doesNotThrowAnyException();
    }

    @Test
    void sacrificingAnotherSerpentGrantsIndestructibleUntilEndOfTurn() {
        Permanent koma = addReadyKoma();
        harness.addToBattlefieldAndReturn(player1, serpent());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(koma.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(koma.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    void cannotSacrificeKomaAsAnotherSerpent() {
        addReadyKoma();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapModeRequiresPermanentTarget() {
        addReadyKoma();
        harness.addToBattlefieldAndReturn(player1, serpent());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent");
    }

    private Permanent addReadyKoma() {
        return addCreatureReady(player1, new KomaCosmosSerpent());
    }

    private static Card serpent() {
        Card card = new Card();
        card.setName("Serpent");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLUE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SERPENT));
        return card;
    }
}
