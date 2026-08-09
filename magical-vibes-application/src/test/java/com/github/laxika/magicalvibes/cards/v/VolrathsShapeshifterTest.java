package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VolrathsShapeshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Keeps its printed characteristics when the graveyard top is not a creature")
    void keepsPrintedCharacteristicsWithoutCreatureOnTop() {
        Permanent shapeshifter = addReadyShapeshifter(player1);
        harness.setGraveyard(player1, List.of(new Forest()));

        gqs.computeStaticBonus(gd, shapeshifter);

        assertThat(shapeshifter.getCard().getName()).isEqualTo("Volrath's Shapeshifter");
        assertThat(shapeshifter.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Uses the full text and characteristics of the top creature card")
    void copiesTopCreatureCard() {
        Permanent shapeshifter = addReadyShapeshifter(player1);
        harness.setGraveyard(player1, List.of(new ProdigalPyromancer()));

        gqs.computeStaticBonus(gd, shapeshifter);

        assertThat(shapeshifter.getCard().getName()).isEqualTo("Prodigal Pyromancer");
        assertThat(shapeshifter.getCard().getPower()).isEqualTo(1);
        assertThat(shapeshifter.getCard().getToughness()).isEqualTo(1);
        assertThat(shapeshifter.getCard().getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("Tracks changes to the top creature card")
    void tracksTopCreatureChanges() {
        Permanent shapeshifter = addReadyShapeshifter(player1);
        harness.setGraveyard(player1, List.of(new ProdigalPyromancer()));

        gqs.computeStaticBonus(gd, shapeshifter);
        assertThat(shapeshifter.getCard().getName()).isEqualTo("Prodigal Pyromancer");

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        graveyard.clear();
        graveyard.add(new GrizzlyBears());
        gqs.computeStaticBonus(gd, shapeshifter);

        assertThat(shapeshifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(shapeshifter.getCard().getActivatedAbilities()).hasSize(1);
    }

    private Permanent addReadyShapeshifter(Player player) {
        Permanent permanent = new Permanent(new VolrathsShapeshifter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
