package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.c.ChainersEdict;
import com.github.laxika.magicalvibes.cards.c.CabalTorturer;
import com.github.laxika.magicalvibes.cards.c.CripplingFatigue;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HydromorphGull.class, BaskingRootwalla.class, CripplingFatigue.class, ChainersEdict.class,
        CabalTorturer.class})
class HydromorphGullTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting a creature you control")
    void countersSpellTargetingYourCreature() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        harness.addToBattlefield(player1, new HydromorphGull());

        CripplingFatigue fatigue = new CripplingFatigue();
        harness.setHand(player2, List.of(fatigue));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, rootwalla.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 1, null, fatigue.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crippling Fatigue");
        harness.assertOnBattlefield(player1, "Basking Rootwalla");
        harness.assertInGraveyard(player1, "Hydromorph Gull");
    }

    @Test
    @DisplayName("Cannot target a spell that targets a creature you do not control")
    void cannotTargetSpellTargetingOpponentsCreature() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player2, new BaskingRootwalla());
        harness.addToBattlefield(player1, new HydromorphGull());

        CripplingFatigue fatigue = new CripplingFatigue();
        harness.setHand(player1, List.of(fatigue));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, rootwalla.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fatigue.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell that targets a player")
    void cannotTargetSpellTargetingPlayer() {
        harness.addToBattlefield(player1, new HydromorphGull());

        ChainersEdict edict = new ChainersEdict();
        harness.setHand(player2, List.of(edict));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, edict.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell with no targets")
    void cannotTargetNonTargetingSpell() {
        harness.addToBattlefield(player1, new HydromorphGull());

        BaskingRootwalla rootwalla = new BaskingRootwalla();
        harness.setHand(player2, List.of(rootwalla));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rootwalla.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability even when it targets a creature you control")
    void cannotTargetActivatedAbility() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        harness.addToBattlefield(player1, new HydromorphGull());
        addCreatureReady(player2, new CabalTorturer());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, rootwalla.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 1, null, gd.stack.getLast().getTargetableId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
