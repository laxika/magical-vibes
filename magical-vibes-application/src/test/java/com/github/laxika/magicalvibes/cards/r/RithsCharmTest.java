package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MagmaBurst;
import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RithsCharm.class, TerminalMoraine.class, Mountain.class, MoggJailer.class, MagmaBurst.class})
class RithsCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 destroys a target nonbasic land")
    void destroysNonbasicLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Terminal Moraine"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Terminal Moraine");
    }

    @Test
    @DisplayName("Mode 0 cannot target a basic land")
    void rejectsBasicLandTarget() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player2, "Mountain")))
                .hasMessageContaining("nonbasic land");
    }

    @Test
    @DisplayName("Mode 0 cannot target a nonland permanent")
    void rejectsNonLandTarget() {
        harness.addToBattlefield(player2, new MoggJailer());
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player2, "Mogg Jailer")))
                .hasMessageContaining("nonbasic land");
    }

    @Test
    @DisplayName("Mode 1 creates three Saproling tokens")
    void createsSaprolings() {
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(3)
                .allSatisfy(saproling -> {
                    assertThat(saproling.getCard().getName()).isEqualTo("Saproling");
                    assertThat(saproling.getCard().getPower()).isEqualTo(1);
                    assertThat(saproling.getCard().getToughness()).isEqualTo(1);
                    assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                    assertThat(saproling.getCard().getKeywords()).isEmpty();
                    assertThat(saproling.getCard().isToken()).isTrue();
                });
    }

    @Test
    @DisplayName("Mode 2 prevents all damage from the chosen source this turn")
    void preventsAllDamageFromChosenSource() {
        harness.setLife(player1, 20);
        Permanent chosenSource = addCreatureReady(player2, new MoggJailer());
        Permanent otherSource = addCreatureReady(player2, new MoggJailer());
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Mode 2 prevents all damage from a chosen spell on the stack this turn")
    void preventsAllDamageFromChosenSpellOnStack() {
        harness.setLife(player2, 20);
        MagmaBurst magmaBurst = new MagmaBurst();
        harness.setHand(player1, List.of(magmaBurst, new RithsCharm()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, magmaBurst.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

}
