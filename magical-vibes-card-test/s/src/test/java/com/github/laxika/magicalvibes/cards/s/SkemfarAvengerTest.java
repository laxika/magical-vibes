package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodlinePretender;
import com.github.laxika.magicalvibes.cards.b.BreakneckBerserker;
import com.github.laxika.magicalvibes.cards.b.BrinebarrowIntruder;
import com.github.laxika.magicalvibes.cards.c.CripplingFear;
import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.l.LysAlanaScarblade;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkemfarAvenger.class, LysAlanaScarblade.class, ElvishBerserker.class,
        GrizzlyBears.class, Shock.class, BloodlinePretender.class, BreakneckBerserker.class,
        BrinebarrowIntruder.class, CripplingFear.class, DemonBolt.class, JasperaSentinel.class,
        WingsOfVelisVel.class})
class SkemfarAvengerTest extends BaseCardTest {

    private void killWithShock(Player targetController, String targetName) {
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private void addElfToken() {
        Card tokenCard = new Card();
        tokenCard.setName("Elf Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setSubtypes(List.of(CardSubtype.ELF));
        harness.addToBattlefieldAndReturn(player1, tokenCard);
    }

    @Test
    @DisplayName("A nontoken Elf dying draws a card and causes 1 life loss")
    void nontokenElfDyingTriggers() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new LysAlanaScarblade());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Lys Alana Scarblade");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A nontoken Berserker dying triggers")
    void nontokenBerserkerDyingTriggers() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new ElvishBerserker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Elvish Berserker");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A nontoken creature without Elf or Berserker does not trigger")
    void unrelatedCreatureDyingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Elf token dying does not trigger")
    void tokenElfDyingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        addElfToken();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Elf Token");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Berserker that is not an Elf triggers once")
    void berserkerWithoutElfTypeDyingTriggers() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new BreakneckBerserker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JasperaSentinel(), new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Breakneck Berserker");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Breakneck Berserker");
    }

    @Test
    @DisplayName("Skemfar Avenger does not trigger for its own death")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Skemfar Avenger");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Skemfar Avenger");
    }

    @Test
    @DisplayName("An opposing Elf dying does not trigger")
    void opposingElfDyingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player2, new JasperaSentinel());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player2, "Jaspera Sentinel");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player2, "Jaspera Sentinel");
    }

    @Test
    @DisplayName("Another Skemfar Avenger dying triggers only the surviving Avenger")
    void anotherAvengerDyingTriggersOnce() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        Permanent dyingAvenger = harness.addToBattlefieldAndReturn(player1, new SkemfarAvenger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JasperaSentinel(), new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, dyingAvenger.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each qualifying simultaneous death triggers even when Skemfar Avenger also dies")
    void simultaneousDeathsTriggerForEachOtherQualifyingCreature() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new BreakneckBerserker());
        harness.setLibrary(player1, List.of(new JasperaSentinel(), new JasperaSentinel(),
                new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new CripplingFear(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("A nontoken changeling dying triggers once despite having both qualifying types")
    void nontokenChangelingDyingTriggersOnce() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new BloodlinePretender());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JasperaSentinel(), new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Bloodline Pretender");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Bloodline Pretender");
    }

    @Test
    @DisplayName("Creature types gained until end of turn count immediately before death")
    void gainedCreatureTypesAtDeathTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        Permanent intruder = harness.addToBattlefieldAndReturn(player1, new BrinebarrowIntruder());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.setLibrary(player1, List.of(new JasperaSentinel(), new JasperaSentinel()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, intruder.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, intruder, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, intruder, CardSubtype.BERSERKER)).isTrue();
        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, intruder.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Brinebarrow Intruder");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore - 1);
    }
}
