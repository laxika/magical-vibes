package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelAlliance.class, CrawWurm.class, GrizzlyBears.class})
class CruelAllianceTest extends BaseCardTest {

    @Test
    void exilesCreatureWithManaValueThreeOrLessWithoutTeamwork() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(target, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting("id").containsExactly(target.getCard().getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void exilesAnyCreatureAndGainsLifeWhenTeamworkIsPaid() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting("id").containsExactly(target.getCard().getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void cannotTargetCreatureWithManaValueAboveThreeWithoutTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        harness.setHand(player1, List.of(new CruelAlliance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    private void cast(Permanent target, List<java.util.UUID> teamworkPermanents) {
        harness.setHand(player1, List.of(new CruelAlliance()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorceryWithSacrifices(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }
}
