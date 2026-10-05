package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JolraelVoiceOfZhalfir;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.cards.r.RebornHope;
import com.github.laxika.magicalvibes.cards.r.RebuildTheCity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivMizzetSupreme.class, Putrefy.class, Shock.class, Plains.class, GrizzlyBears.class,
        MycosynthLattice.class, RebornHope.class, RebuildTheCity.class, JolraelVoiceOfZhalfir.class})
class NivMizzetSupremeTest extends BaseCardTest {

    @Test
    @DisplayName("Niv-Mizzet grants jump-start to exactly two-color instants and sorceries")
    void grantsJumpStartToExactlyTwoColorInstant() {
        Putrefy spell = new Putrefy();
        Plains discarded = new Plains();
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castJumpStart(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Niv-Mizzet does not grant jump-start to monocolored cards")
    void doesNotGrantJumpStartToMonocoloredCard() {
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Plains()));

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    void opponentMonocoloredSpellCannotTarget() {
        var niv = harness.addToBattlefieldAndReturn(player2, new NivMizzetSupreme());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, niv.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Niv-Mizzet, Supreme");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void opponentMulticoloredSpellCanTarget() {
        var niv = harness.addToBattlefieldAndReturn(player2, new NivMizzetSupreme());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, niv.getId());

        harness.assertNotOnBattlefield(player2, "Niv-Mizzet, Supreme");
        harness.assertInGraveyard(player2, "Niv-Mizzet, Supreme");
    }

    @Test
    void controllersMonocoloredSpellCanTarget() {
        var niv = harness.addToBattlefieldAndReturn(player1, new NivMizzetSupreme());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, niv.getId());

        assertThat(niv.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Niv-Mizzet, Supreme");
    }

    @Test
    void opponentColorlessSpellCanTarget() {
        var niv = harness.addToBattlefieldAndReturn(player2, new NivMizzetSupreme());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, niv.getId());

        harness.assertInGraveyard(player2, "Niv-Mizzet, Supreme");
        harness.assertNotOnBattlefield(player2, "Niv-Mizzet, Supreme");
    }

    @Test
    void colorlessGraveyardCardDoesNotGainJumpStart() {
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        harness.addToBattlefield(player1, new MycosynthLattice());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Putrefy()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Putrefy");
    }

    @Test
    void doesNotGrantJumpStartToOpponentsGraveyard() {
        harness.addToBattlefield(player2, new NivMizzetSupreme());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Putrefy()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    void jumpStartRequiresDiscardingACard() {
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Putrefy()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
        harness.assertInGraveyard(player1, "Putrefy");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void grantsJumpStartToTwoColorSorcery() {
        var spell = new RebornHope();
        var returned = new Putrefy();
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        harness.setGraveyard(player1, List.of(spell, returned));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castJumpStart(player1, 0, 0, returned.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Putrefy");
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void doesNotGrantJumpStartToThreeColorSorcery() {
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        var land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setGraveyard(player1, List.of(new RebuildTheCity()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new NivMizzetSupreme());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantJumpStartToTwoColorCreature() {
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        harness.setGraveyard(player1, List.of(new JolraelVoiceOfZhalfir()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    void jumpStartDoesNotBypassSorceryTiming() {
        harness.addToBattlefield(player1, new NivMizzetSupreme());
        var returned = new Putrefy();
        harness.setGraveyard(player1, List.of(new RebornHope(), returned));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, returned.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell");
        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Reborn Hope");
    }
}
