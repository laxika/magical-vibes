package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeliodSunCrowned;
import com.github.laxika.magicalvibes.cards.l.LivingDeath;
import com.github.laxika.magicalvibes.cards.m.Mycoloth;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContainmentPriest.class, GatherTheTownsfolk.class, GrizzlyBears.class,
        Mycoloth.class, Zombify.class, HeliodSunCrowned.class, LivingDeath.class, TurnToFrog.class})
class ContainmentPriestTest extends BaseCardTest {

    private void castContainmentPriest(Player controller) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(controller, new ContainmentPriest(), "{1}{W}");
        harness.passBothPriorities();
    }

    private void reanimate(Player caster, Player graveyardOwner) {
        Card target = gd.playerGraveyards.get(graveyardOwner.getId()).getFirst();
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new Zombify()));
        harness.addMana(caster, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(caster, 0, target.getId());
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Human".equals(permanent.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("A nontoken creature entering without being cast is exiled")
    void reanimatedCreatureIsExiled() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castContainmentPriest(player1);

        reanimate(player2, player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Grizzly Bears");
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("An exiled creature does not offer devour or modify existing permanents")
    void exiledCreatureDoesNotDevour() {
        harness.setGraveyard(player2, List.of(new Mycoloth()));
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castContainmentPriest(player1);

        reanimate(player2, player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstBear, secondBear);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Mycoloth");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature tokens enter normally")
    void tokensStillEnter() {
        castContainmentPriest(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature spell enters normally")
    void castCreatureStillEnters() {
        castContainmentPriest(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Priest entering without being cast does not exile itself")
    void reanimatedPriestDoesNotExileItself() {
        harness.setGraveyard(player1, List.of(new ContainmentPriest()));

        reanimate(player1, player1);

        harness.assertOnBattlefield(player1, "Containment Priest");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Priest does not affect creatures entering simultaneously with it")
    void simultaneousReanimationDoesNotExileOtherCreatures() {
        harness.setGraveyard(player1, List.of(new ContainmentPriest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new LivingDeath(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Containment Priest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Priest without abilities does not exile entering creatures")
    void abilityRemovalDisablesReplacement() {
        castContainmentPriest(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Containment Priest"));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        reanimate(player2, player2);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Heliod with insufficient devotion enters as a noncreature")
    void noncreatureGodIsNotExiled() {
        castContainmentPriest(player1);
        harness.setGraveyard(player2, List.of(new HeliodSunCrowned()));

        reanimate(player2, player2);

        harness.assertOnBattlefield(player2, "Heliod, Sun-Crowned");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Heliod entering with sufficient devotion is exiled")
    void creatureGodIsExiled() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new ContainmentPriest());
        }
        HeliodSunCrowned heliod = new HeliodSunCrowned();
        harness.setGraveyard(player2, List.of(heliod));

        reanimate(player2, player2);

        harness.assertNotOnBattlefield(player2, "Heliod, Sun-Crowned");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(heliod.getId());
    }

    @Test
    @DisplayName("Priest also replaces its controller's uncast creatures")
    void ownReanimatedCreatureIsExiled() {
        castContainmentPriest(player1);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        reanimate(player1, player1);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(bear.getId());
    }

    @Test
    @DisplayName("Priest can be flashed in response to a reanimation spell")
    void flashInResponseExilesReanimationTarget() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bear));
        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castSorcery(player2, 0, bear.getId());

        harness.castFromHand(player1, new ContainmentPriest(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Containment Priest");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(bear.getId());
    }
}
