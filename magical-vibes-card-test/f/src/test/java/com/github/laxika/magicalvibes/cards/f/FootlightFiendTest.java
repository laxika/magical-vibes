package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CryOfTheCarnarium;
import com.github.laxika.magicalvibes.cards.d.DomriChaosBringer;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FootlightFiend.class, LlanowarElves.class, Shock.class, DomriChaosBringer.class,
        CryOfTheCarnarium.class})
class FootlightFiendTest extends BaseCardTest {

    @Test
    @DisplayName("When Footlight Fiend dies, it deals 1 damage to a target player")
    void deathTriggerDamagesTargetPlayer() {
        harness.addToBattlefield(player1, new FootlightFiend());
        int lifeBefore = gd.getLife(player2.getId());

        killFootlightFiend();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("When Footlight Fiend dies, it deals 1 damage to a target creature")
    void deathTriggerDamagesTargetCreature() {
        harness.addToBattlefield(player1, new FootlightFiend());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        killFootlightFiend();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The death trigger can damage its controller")
    void deathTriggerCanDamageController() {
        harness.addToBattlefield(player1, new FootlightFiend());
        int lifeBefore = gd.getLife(player1.getId());

        killFootlightFiend();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("The dying creature's controller chooses the target even when an opponent kills it")
    void opponentControlsTheirOwnDeathTrigger() {
        harness.addToBattlefield(player2, new FootlightFiend());
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Footlight Fiend"));
        harness.assertInGraveyard(player2, "Footlight Fiend");
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("The death trigger removes one loyalty from a targeted planeswalker")
    void deathTriggerDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new FootlightFiend());
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriChaosBringer());
        domri.setCounterCount(CounterType.LOYALTY, 5);

        killFootlightFiend();
        harness.handlePermanentChosen(player1, domri.getId());
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Domri, Chaos Bringer");
    }

    @Test
    @DisplayName("The death trigger does not deal damage when its target leaves before resolution")
    void deathTriggerDoesNotRetargetWhenCreatureLeaves() {
        harness.addToBattlefield(player1, new FootlightFiend());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        int life1Before = gd.getLife(player1.getId());
        int life2Before = gd.getLife(player2.getId());

        killFootlightFiend();
        harness.handlePermanentChosen(player1, targetId);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.passBothPriorities();

        harness.assertLife(player1, life1Before);
        harness.assertLife(player2, life2Before);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile replacing death does not trigger Footlight Fiend")
    void exileInsteadOfDeathDoesNotTrigger() {
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new FootlightFiend());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Footlight Fiend"));

        harness.assertNotOnBattlefield(player1, "Footlight Fiend");
        harness.assertNotInGraveyard(player1, "Footlight Fiend");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Footlight Fiend"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void killFootlightFiend() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Footlight Fiend");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertInGraveyard(player1, "Footlight Fiend");
    }
}
