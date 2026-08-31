package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.v.VodalianArcanist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CormelaGlamourThief.class, LightningBolt.class, VodalianArcanist.class,
        Murder.class, Opt.class, GrizzlyBears.class})
class CormelaGlamourThiefTest extends BaseCardTest {

    @Test
    void tapAddsThreeColorsOfInstantOrSorceryOnlyMana() {
        Permanent cormela = addCreatureReady(player1, new CormelaGlamourThief());

        harness.activateAbility(player1, 0, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(cormela.isTapped()).isTrue();
    }

    @Test
    void instantOrSorceryOnlyManaCannotCastCreatureSpells() {
        addCreatureReady(player1, new CormelaGlamourThief());
        harness.setHand(player1, List.of(new VodalianArcanist()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathTriggerReturnsTargetInstantOrSorceryFromGraveyard() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        Opt opt = new Opt();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(opt, bears));

        destroyCormela(cormela);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Opt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void deathTriggerCanDeclineItsOptionalReturn() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(opt));

        destroyCormela(cormela);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        harness.assertNotInHand(player1, "Opt");
    }

    private void destroyCormela(Permanent cormela) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, cormela.getId());
        harness.passBothPriorities();
    }
}
