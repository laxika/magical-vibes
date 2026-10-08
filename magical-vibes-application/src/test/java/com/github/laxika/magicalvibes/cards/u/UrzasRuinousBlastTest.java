package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.b.BlessedLight;
import com.github.laxika.magicalvibes.cards.d.DampingSphere;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.j.JayaBallard;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasRuinousBlast.class, ArvadTheCursed.class, GrizzlyBears.class,
        HonorOfThePure.class, Plains.class, BlessedLight.class, DampingSphere.class,
        JayaBallard.class, MoxAmber.class})
class UrzasRuinousBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles nonlegendary creatures on both sides")
    void exilesNonlegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Nonlegendary creatures should be exiled
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Exiled, not in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Legendary creatures survive")
    void legendaryCreaturesSurvive() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
    }

    @Test
    @DisplayName("Lands survive")
    void landsSurvive() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles nonlegendary enchantments")
    void exilesNonlegendaryEnchantments() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new HonorOfThePure());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Honor of the Pure");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Honor of the Pure"));
    }

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendary() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Urza's Ruinous Blast goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new ArvadTheCursed());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Urza's Ruinous Blast");
    }

    @Test
    void legendaryPlaneswalkerEnablesCastingAndSurvives() {
        harness.addToBattlefield(player1, new JayaBallard());
        harness.addToBattlefield(player2, new MoxAmber());
        harness.addToBattlefield(player2, new DampingSphere());
        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Jaya Ballard");
        harness.assertOnBattlefield(player2, "Mox Amber");
        harness.assertNotOnBattlefield(player2, "Damping Sphere");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Damping Sphere"));
    }

    @Test
    void legendaryArtifactAndOpponentsLegendaryCreatureDoNotEnableCasting() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player2, new ArvadTheCursed());
        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesAfterQualifyingLegendaryCreatureLeavesBattlefield() {
        var arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new DampingSphere());
        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.setHand(player2, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player2, 0, arvad.getId());
        harness.assertNotOnBattlefield(player1, "Arvad the Cursed");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Damping Sphere");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Damping Sphere"));
        harness.assertInGraveyard(player1, "Urza's Ruinous Blast");
    }
}
