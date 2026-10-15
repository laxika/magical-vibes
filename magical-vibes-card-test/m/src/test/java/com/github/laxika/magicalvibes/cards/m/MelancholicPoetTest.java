package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeBlast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MelancholicPoet.class, HillGiant.class, Shock.class, TomeBlast.class})
class MelancholicPoetTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature drains the opponent for 1")
    void reparteeDrainsOnCreatureTarget() {
        harness.addToBattlefield(player1, new MelancholicPoet());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new MelancholicPoet());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("A sorcery targeting your own creature triggers before the spell resolves")
    void sorceryTargetingOwnCreatureTriggers() {
        UUID poetId = harness.addToBattlefieldAndReturn(player1, new MelancholicPoet()).getId();
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, poetId);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Melancholic Poet");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Melancholic Poet");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a targeted sorcery with flashback also triggers Repartee")
    void flashbackTriggersRepartee() {
        harness.addToBattlefield(player1, new MelancholicPoet());
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setGraveyard(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveFlashback(player1, 0, giantId);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent casting a creature-targeting spell does not trigger Repartee")
    void opponentSpellDoesNotTrigger() {
        UUID poetId = harness.addToBattlefieldAndReturn(player1, new MelancholicPoet()).getId();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, poetId);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Melancholic Poet");
    }

    @Test
    @DisplayName("A pending Repartee trigger resolves after the Poet leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        UUID poetId = harness.addToBattlefieldAndReturn(player1, new MelancholicPoet()).getId();
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giantId);
        harness.castAndResolveInstant(player2, 0, poetId);

        harness.assertInGraveyard(player1, "Melancholic Poet");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each Poet triggers separately for the same spell")
    void multiplePoetsTriggerIndependently() {
        harness.addToBattlefield(player1, new MelancholicPoet());
        harness.addToBattlefield(player1, new MelancholicPoet());
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Repartee")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MelancholicPoet());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A sorcery targeting a player does not trigger Repartee")
    void sorceryTargetingPlayerDoesNotTrigger() {
        harness.addToBattlefield(player1, new MelancholicPoet());
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
