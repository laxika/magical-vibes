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
import com.github.laxika.magicalvibes.model.Keyword;
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

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, engine)).isFalse();
    }

    @Test
    void draftChoiceHappensDuringSpellResolutionWithoutAnEtbTrigger() {
        harness.castFromHand(player1, new ProteanWarEngine(), "{R}{W}");
        harness.passBothPriorities();

        PendingInteraction.ProteanWarEngineSpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ProteanWarEngineSpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(choice.sourcePermanentId()))
                .containsExactly(choice.cards().getFirst());
    }

    @Test
    void copyingUsesTheExiledCreaturesPowerToughnessAndKeywords() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ProteanWarEngine());
        gd.addToExile(player1.getId(), new SerraAngel(), engine.getId());
        Permanent crewer = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(crewer.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engine, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, engine, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.isArtifact(gd, engine)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, engine, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, engine, CardSubtype.ANGEL)).isTrue();
    }

    @Test
    void crewingWithoutAnExiledCardStillAnimatesTheVehicle() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ProteanWarEngine());
        Permanent crewer = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, engine)).isTrue();
        assertThat(gqs.getEffectivePower(gd, engine)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engine, Keyword.FLYING)).isFalse();
        assertThat(gd.getCardsExiledByPermanent(engine.getId())).isEmpty();
    }
}
