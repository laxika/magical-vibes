package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BessieTheDoctorsRoadster.class, ArvadTheCursed.class, GrizzlyBears.class})
class BessieTheDoctorsRoadsterTest extends BaseCardTest {

    @Test
    void crewAnimatesBessieAndTapsCrew() {
        Permanent bessie = addReadyPermanent(new BessieTheDoctorsRoadster());
        Permanent crew = addReadyPermanent(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bessie)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackTriggerTargetsAnotherLegendaryCreature() {
        Permanent bessie = addReadyPermanent(new BessieTheDoctorsRoadster());
        Permanent legendaryCreature = addReadyPermanent(new ArvadTheCursed());
        Permanent nonlegendaryCreature = addReadyPermanent(new GrizzlyBears());
        bessie.setAnimatedUntilEndOfTurn(true);
        bessie.setAnimatedPower(3);
        bessie.setAnimatedToughness(4);

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactly(legendaryCreature.getId())
                .doesNotContain(bessie.getId(), nonlegendaryCreature.getId());

        harness.handlePermanentChosen(player1, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(legendaryCreature.isCantBeBlocked()).isTrue();
        assertThat(nonlegendaryCreature.isCantBeBlocked()).isFalse();
        assertThat(bessie.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
