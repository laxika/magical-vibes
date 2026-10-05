package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImpactTremors;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nyx.class, GrizzlyBears.class, ElvishArchdruid.class, LlanowarElves.class,
        ImpactTremors.class, SolRing.class})
class NyxTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Nyx(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void nontokenCreaturesAreAlsoEnchantments() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        assertThat(gqs.isCreature(gd, ownCreature)).isTrue();
        assertThat(gqs.isEnchantment(gd, ownCreature)).isTrue();
        assertThat(gqs.isEnchantment(gd, opposingCreature)).isTrue();
        assertThat(gqs.isEnchantment(gd, token)).isFalse();
    }

    @Test
    void constellationGainsLifeWhenAnEnchantmentEnters() {
        int life = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(life + 1);
    }

    @Test
    void chaosAddsManaEqualToChosenColorDevotion() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "GREEN");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void constellationTriggersForAnEnchantmentToken() {
        int life = gd.getLife(player1.getId());
        ImpactTremors token = new ImpactTremors();
        token.setToken(true);

        harness.enterBattlefieldAndReturn(player1, token);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(life + 1);
    }

    @Test
    void constellationTriggersForANoncreatureEnchantment() {
        int life = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new ImpactTremors());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(life + 1);
    }

    @Test
    void constellationDoesNotTriggerForAnOpponentsNontokenCreature() {
        int ownLife = gd.getLife(player1.getId());
        int opposingLife = gd.getLife(player2.getId());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife);
    }

    @Test
    void constellationStillResolvesAfterNyxLeaves() {
        int life = gd.getLife(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.planechase.faceUp.clear();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(life + 1);
    }

    @Test
    void constellationDoesNotTriggerForAnOpponentsEnchantment() {
        int ownLife = gd.getLife(player1.getId());
        int opposingLife = gd.getLife(player2.getId());

        harness.enterBattlefieldAndReturn(player2, new ImpactTremors());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife);
    }

    @Test
    void constellationDoesNotTriggerForAnOrdinaryCreatureToken() {
        int life = gd.getLife(player1.getId());
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);

        harness.enterBattlefieldAndReturn(player1, token);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(life);
    }

    @Test
    void noncreatureArtifactsDoNotBecomeEnchantmentsOrTriggerConstellation() {
        int life = gd.getLife(player1.getId());

        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new SolRing());
        resolveAllTriggers();

        assertThat(gqs.isEnchantment(gd, artifact)).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(life);
    }

    @Test
    void leavingNyxRemovesTheGrantedEnchantmentType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.isEnchantment(gd, creature)).isTrue();

        gd.planechase.faceUp.clear();

        assertThat(gqs.isEnchantment(gd, creature)).isFalse();
        assertThat(gqs.isCreature(gd, creature)).isTrue();
    }

    @Test
    void chaosIgnoresOpponentsDevotion() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        harness.handleListChoice(player1, "GREEN");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void chaosCanChooseAColorWithZeroDevotion() {
        harness.addToBattlefield(player1, new ElvishArchdruid());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    void chaosUsesDevotionAtResolutionAndSurvivesNyxLeaving() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.inMutationScope(() -> planar.chaos(gd));

        harness.addToBattlefield(player1, new ElvishArchdruid());
        gd.planechase.faceUp.clear();
        resolveAllTriggers();
        harness.handleListChoice(player1, "GREEN");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }
}
