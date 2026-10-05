package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MartyrsBond.class, GrizzlyBears.class, FountainOfYouth.class, AngelicChorus.class, Shock.class,
        Disenchant.class, Forest.class, GloriousAnthem.class, LotusPetal.class, Shatter.class,
        StoneRain.class, Ornithopter.class, PlanarCleansing.class})
class MartyrsBondTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a permanent sharing the dead permanent's card type")
    void opponentSacrificesMatchingPermanentType() {
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new MartyrsBond());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dyingCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The Bond triggers when it is put into a graveyard")
    void sourceDeathTriggers() {
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new MartyrsBond());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bond));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Martyr's Bond");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }


    @Test
    @DisplayName("Makes each opponent sacrifice a permanent sharing a type with a dead artifact")
    void sacrificesMatchingArtifact() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player1, new LotusPetal());
        harness.addToBattlefield(player2, new LotusPetal());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyPermanent(player1, "Lotus Petal", player2, new Shatter(), ManaColor.RED, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lotus Petal");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when another land you control dies")
    void doesNotTriggerForLand() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyPermanent(player1, "Forest", player2, new StoneRain(), ManaColor.RED, 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers when Martyr's Bond itself is put into a graveyard")
    void sacrificesMatchingPermanentWhenItDies() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyPermanent(player1, "Martyr's Bond", player2, new Disenchant(), ManaColor.WHITE, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A permanent with multiple card types requires only one sacrifice of either type")
    void multipleTypesAllowEitherTypeButOnlyOneSacrifice() {
        harness.addToBattlefield(player1, new MartyrsBond());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent with no matching permanent sacrifices nothing")
    void noMatchingPermanentDoesNotSacrificeAnotherType() {
        harness.addToBattlefield(player1, new MartyrsBond());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("An opponent's permanent dying does not trigger Bond")
    void opponentsPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MartyrsBond());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An ally death trigger resolves after Bond leaves the battlefield")
    void allyDeathTriggerSurvivesBondLeaving() {
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new MartyrsBond());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new Shock(), new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.castAndResolveInstant(player1, 0, bond.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bond dying with another nonland permanent triggers once for each")
    void simultaneousDeathsTriggerOncePerPermanent() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Martyr's Bond");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void destroyPermanent(Player player, String name, Player caster, Card spell,
            ManaColor manaColor, int manaAmount) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(spell));
        harness.addMana(caster, manaColor, manaAmount);
        UUID permanentId = harness.getPermanentId(player, name);
        if (spell instanceof Shatter || spell instanceof Disenchant) {
            harness.castAndResolveInstant(caster, 0, permanentId);
        } else {
            harness.castAndResolveSorcery(caster, 0, permanentId);
        }
    }
}
