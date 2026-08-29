package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResculptTest extends BaseCardTest {

    @Test
    void exilesCreatureAndCreatesElementalForItsController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Resculpt()));
        addMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental")
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().getColors().size() == 2
                        && permanent.getCard().getColors().containsAll(List.of(CardColor.BLUE, CardColor.RED))
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getPower() == 4
                        && permanent.getCard().getToughness() == 4
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ELEMENTAL));
    }

    @Test
    void exilesNoncreatureArtifactAndCreatesElementalForItsController() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new Resculpt()));
        addMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Millstone"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Millstone"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental")
                        && permanent.getCard().getPower() == 4
                        && permanent.getCard().getToughness() == 4);
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Resculpt()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
