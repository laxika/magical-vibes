package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MausoleumWanderer.class, ChapelGeist.class, GrizzlyBears.class, Shock.class,
        BumpInTheNight.class})
class MausoleumWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Mausoleum Wanderer does not trigger for its own entry")
    void ownEntryDoesNotBoost() {
        harness.castFromHand(player1, new MausoleumWanderer(), "{U}");
        harness.passBothPriorities();

        Permanent wanderer = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wanderer.getPowerModifier()).isZero();
        assertThat(wanderer.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Spirit does not boost Mausoleum Wanderer")
    void opposingSpiritDoesNotBoost() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MausoleumWanderer(), "{U}");
        harness.passBothPriorities();

        assertThat(wanderer.getPowerModifier()).isZero();
        assertThat(wanderer.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each other Spirit entry creates its own cumulative boost")
    void multipleSpiritEntriesBoostSeparately() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        for (int i = 0; i < 2; i++) {
            castChapelGeist(player1);
            harness.passBothPriorities();
            assertThat(wanderer.getPowerModifier()).isEqualTo(i);
            harness.passBothPriorities();
            assertThat(wanderer.getPowerModifier()).isEqualTo(i + 1);
            assertThat(wanderer.getToughnessModifier()).isEqualTo(i + 1);
        }
    }

    @Test
    @DisplayName("Sacrifice counters a sorcery and is paid before the ability resolves")
    void countersSorcery() {
        harness.addToBattlefield(player1, new MausoleumWanderer());
        BumpInTheNight spell = new BumpInTheNight();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.assertNotOnBattlefield(player1, "Mausoleum Wanderer");
        harness.assertInGraveyard(player1, "Mausoleum Wanderer");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bump in the Night");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell is countered when its controller can pay but declines")
    void decliningPaymentCountersSpell() {
        harness.addToBattlefield(player1, new MausoleumWanderer());
        Shock spell = new Shock();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Spirit entering gives Mausoleum Wanderer +1/+1 until end of turn")
    void anotherSpiritEnteringBoosts() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        castChapelGeist(player1);
        harness.passBothPriorities(); // resolve Geist
        harness.passBothPriorities(); // resolve Wanderer trigger

        assertThat(wanderer.getPowerModifier()).isEqualTo(1);
        assertThat(wanderer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Spirit creature entering does not boost Mausoleum Wanderer")
    void nonSpiritDoesNotBoost() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(wanderer.getPowerModifier()).isEqualTo(0);
        assertThat(wanderer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The Spirit ETB boost wears off at end of turn")
    void spiritBoostWearsOffAtCleanup() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        castChapelGeist(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(wanderer.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wanderer.getPowerModifier()).isEqualTo(0);
        assertThat(wanderer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrifice counters an instant when controller cannot pay power mana")
    void countersInstantWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new MausoleumWanderer());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1); // cast only

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mausoleum Wanderer");
    }

    @Test
    @DisplayName("Boosted power raises the counter-unless-pays cost")
    void boostedPowerRaisesPayAmount() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        castChapelGeist(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(wanderer.getPowerModifier()).isEqualTo(1); // 2 power

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        // 1 to cast + 1 floating — not enough to pay {2}
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        // Can't afford {2} with 1 mana left → auto-countered
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castChapelGeist(com.github.laxika.magicalvibes.model.Player controller) {
        harness.castFromHand(controller, new ChapelGeist(), "{1}{W}{W}");
    }

    @Test
    @DisplayName("Spell survives when controller pays the snapshotted power amount")
    void notCounteredWhenControllerPaysPower() {
        harness.addToBattlefield(player1, new MausoleumWanderer());

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 2); // 1 cast + 1 pay

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new MausoleumWanderer());

        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
