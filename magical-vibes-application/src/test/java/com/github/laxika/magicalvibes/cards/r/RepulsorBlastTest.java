package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepulsorBlast.class, CrawWurm.class, GrizzlyBears.class, Unsummon.class})
class RepulsorBlastTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToTargetCreatureWithoutTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());

        cast(target, List.of());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void teamworkAlsoDealsTwoDamageToTargetCreaturesController() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void teamworkDamagesControllerEvenWhenCreatureDies() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void teamworkCanDamageYourOwnCreaturesController() {
        Permanent target = addCreatureReady(player1, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));

        harness.assertInGraveyard(player1, "Craw Wurm");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void summoningSickCreatureCanPayTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        teammate.setSummoningSick(true);

        cast(target, List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void teamworkCanTapMoreCreaturesThanNeeded() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotUseOpponentsCreatureForTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(target, List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(teammate.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotUseAlreadyTappedCreatureForTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        teammate.setTapped(true);

        assertThatThrownBy(() -> cast(target, List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
    }

    @Test
    void removingTheOnlyTargetPreventsControllerDamage() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RepulsorBlast()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(teammate.getId()));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Craw Wurm");
        harness.assertLife(player2, 20);
        assertThat(teammate.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Repulsor Blast");
    }
    private void cast(Permanent target, List<java.util.UUID> teamworkPermanents) {
        harness.setHand(player1, List.of(new RepulsorBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorceryTappingPermanents(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }
}
