package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.cards.h.HolyLight;
import com.github.laxika.magicalvibes.cards.t.TivadarsCrusade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneCalendar.class, BogImp.class, FellwarStone.class, HolyLight.class, TivadarsCrusade.class})
class StoneCalendarTest extends BaseCardTest {

    @Test
    @DisplayName("Spells you cast cost {1} less")
    void spellsYouCastCostOneLess() {
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.setHand(player1, List.of(new BogImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction applies to sorcery spells")
    void sorcerySpellsAreReduced() {
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.setHand(player1, List.of(new TivadarsCrusade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void artifactSpellsAreReduced() {
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.setHand(player1, List.of(new FellwarStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void multipleReductionsStack() {
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.setHand(player1, List.of(new FellwarStone()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction does not pay colored mana")
    void coloredManaIsNotReduced() {
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.setHand(player1, List.of(new HolyLight()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to opponents' spells")
    void opponentsSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StoneCalendar());
        harness.setHand(player2, List.of(new BogImp()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
