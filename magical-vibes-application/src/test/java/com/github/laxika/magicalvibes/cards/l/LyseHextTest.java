package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LyseHext.class, ChromaticStar.class, GrizzlyBears.class, Shock.class})
class LyseHextTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spells cost {1} less to cast")
    void reducesNoncreatureSpellCosts() {
        harness.addToBattlefield(player1, new LyseHext());
        harness.setHand(player1, List.of(new ChromaticStar()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Creature spells do not get the cost reduction")
    void doesNotReduceCreatureSpellCosts() {
        harness.addToBattlefield(player1, new LyseHext());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains double strike after casting two noncreature spells")
    void gainsDoubleStrikeAfterTwoNoncreatureSpells() {
        Permanent lyse = addCreatureReady(player1, new LyseHext());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.hasKeyword(gd, lyse, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, lyse, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, lyse, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
