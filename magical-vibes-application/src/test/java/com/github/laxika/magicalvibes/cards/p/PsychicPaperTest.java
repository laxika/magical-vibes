package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicPaper.class, GrizzlyBears.class, HillGiant.class})
class PsychicPaperTest extends BaseCardTest {

    @Test
    void choosesCreatureNameAndTypeAndGrantsEvasionAndWard() {
        Permanent paper = addReady(player1, new PsychicPaper());
        Permanent host = addReady(player1, new GrizzlyBears());
        addReady(player1, new HillGiant());
        addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Hill Giant");
        harness.handleListChoice(player1, "WIZARD");

        assertThat(gqs.getEffectiveName(gd, host)).isEqualTo("Hill Giant");
        assertThat(gqs.effectiveCreatureSubtypes(gd, host)).containsExactly(CardSubtype.WIZARD);
        assertThat(gqs.hasKeyword(gd, host, Keyword.WARD)).isTrue();

        declareAttackersAndPrepareBlockers(List.of(1));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");

        assertThat(paper.getAttachedTo()).isEqualTo(host.getId());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
