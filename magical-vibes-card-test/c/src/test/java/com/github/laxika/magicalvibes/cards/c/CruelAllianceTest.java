package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

    @Test
    void summoningSickCreatureCanPayTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        teammate.setSummoningSick(true);

        cast(target, List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting("id").containsExactly(target.getCard().getId());
        harness.assertLife(player1, 23);
    }

    @Test
    void targetCanAlsoPayTeamwork() {
        Permanent target = addCreatureReady(player1, new CrawWurm());

        cast(target, List.of(target.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting("id").containsExactly(target.getCard().getId());
        harness.assertLife(player1, 23);
    }

    @Test
    void tappedCreatureCannotPayTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        teammate.tap();
        harness.setHand(player1, List.of(new CruelAlliance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsCreatureCannotPayTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelAlliance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotGainLifeWhenTeamworkTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelAlliance()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(teammate.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target, List<java.util.UUID> teamworkPermanents) {
        harness.setHand(player1, List.of(new CruelAlliance()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorceryWithSacrifices(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }
}
