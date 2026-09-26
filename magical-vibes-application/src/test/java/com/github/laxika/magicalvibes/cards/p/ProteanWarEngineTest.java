package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.b.BladeHistorian;
import com.github.laxika.magicalvibes.cards.c.CaptivatingCrew;
import com.github.laxika.magicalvibes.cards.d.DuelcraftTrainer;
import com.github.laxika.magicalvibes.cards.f.FalconerAdept;
import com.github.laxika.magicalvibes.cards.m.ManaformHellkite;
import com.github.laxika.magicalvibes.cards.m.MoonveilRegent;
import com.github.laxika.magicalvibes.cards.o.OgreBattledriver;
import com.github.laxika.magicalvibes.cards.r.ResplendentAngel;
import com.github.laxika.magicalvibes.cards.s.SeraphOfDawn;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SerraParagon;
import com.github.laxika.magicalvibes.cards.s.SkyshipStalker;
import com.github.laxika.magicalvibes.cards.s.StarCrownedStag;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProteanWarEngine.class, SerraAngel.class, ResplendentAngel.class,
        DuelcraftTrainer.class, FalconerAdept.class, SeraphOfDawn.class,
        StarCrownedStag.class, BenalishMarshal.class, SerraParagon.class,
        BladeHistorian.class, CaptivatingCrew.class, ManaformHellkite.class,
        MoonveilRegent.class, SkyshipStalker.class, OgreBattledriver.class})
class ProteanWarEngineTest extends BaseCardTest {

    @Test
    void draftsThreeSpellbookCardsAndExilesTheChosenCard() {
        Permanent engine = harness.enterBattlefieldAndReturn(player1, new ProteanWarEngine());
        harness.passBothPriorities();

        PendingInteraction.ProteanWarEngineSpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ProteanWarEngineSpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        assertThat(gd.getCardsExiledByPermanent(engine.getId()))
                .containsExactly(choice.cards().getFirst());
    }

    @Test
    void becomesTheExiledCreatureAndKeepsVehicleArtifactTypesUntilEndOfTurn() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ProteanWarEngine());
        engine.setSummoningSick(false);
        SerraAngel exiledCard = new SerraAngel();
        gd.addToExile(player1.getId(), exiledCard, engine.getId());
        harness.addToBattlefieldAndReturn(player1, new SerraAngel()).setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        }
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, engine)).isTrue();
        assertThat(gqs.isArtifact(gd, engine)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, engine, CardSubtype.VEHICLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, engine)).isFalse();
    }
}
