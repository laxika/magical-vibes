package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LukkaCoppercoatOutcast.class, LukkaBoundToRuin.class, GrizzlyBears.class,
        Shock.class, Forest.class, ColossalDreadmaw.class})
class LukkaCoppercoatOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("+1 exiles three cards and lets the controller cast an exiled creature")
    void plusOneGrantsCreatureCastPermission() {
        addReadyLukka(player1, 5);
        Card shock = new Shock();
        Card creature = new GrizzlyBears();
        Card forest = new Forest();
        setLibrary(shock, creature, forest);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
    }

    @Test
    @DisplayName("+1 permission survives the source leaving while another Lukka is controlled")
    void plusOnePermissionUsesAnyControlledLukka() {
        Permanent lukka = addReadyLukka(player1, 5);
        Card creature = new GrizzlyBears();
        setLibrary(creature, new Forest(), new Shock());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(lukka);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent otherLukka = harness.addToBattlefieldAndReturn(player1, new LukkaBoundToRuin());
        otherLukka.setCounterCount(CounterType.LOYALTY, 5);
        otherLukka.setSummoningSick(false);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 exiles a creature and finds a creature with greater mana value")
    void minusTwoReplacesCreatureWithHigherManaValueCreature() {
        addReadyLukka(player1, 5);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card lowerCreature = new GrizzlyBears();
        Card shock = new Shock();
        Card higherCreature = new ColossalDreadmaw();
        Card forest = new Forest();
        setLibrary(lowerCreature, shock, higherCreature, forest);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock", "Forest");
    }

    @Test
    @DisplayName("-7 has each creature deal its power to each opponent")
    void minusSevenDealsEachCreaturePowerToOpponent() {
        Permanent lukka = addReadyLukka(player1, 7);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(lukka);
    }

    @Test
    @DisplayName("-2 uses zero mana value for a face-down creature")
    void minusTwoUsesFaceDownManaValue() {
        addReadyLukka(player1, 5);
        Permanent target = addCreatureReady(player1, new ColossalDreadmaw());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Card replacement = new GrizzlyBears();
        Card remaining = new Forest();
        setLibrary(replacement, remaining);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("-2 returns the entire library when no greater creature exists")
    void minusTwoWithNoGreaterCreature() {
        addReadyLukka(player1, 5);
        Permanent target = addCreatureReady(player1, new ColossalDreadmaw());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        setLibrary(creature, land);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 does not reveal cards when its target leaves before resolution")
    void minusTwoWithMissingTarget() {
        addReadyLukka(player1, 5);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card creature = new ColossalDreadmaw();
        Card land = new Forest();
        setLibrary(creature, land);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, land);
        harness.assertNotOnBattlefield(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("+1 exiles all available cards from a library shorter than three cards")
    void plusOneWithShortLibrary() {
        addReadyLukka(player1, 5);
        Card creature = new GrizzlyBears();
        setLibrary(creature);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("+1 does not grant casting permission to noncreature cards")
    void plusOneDoesNotPermitNoncreatureSpells() {
        addReadyLukka(player1, 5);
        Card shock = new Shock();
        setLibrary(shock);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    @DisplayName("+1 permission does not allow a creature to be cast on an opponent's turn")
    void plusOnePreservesCreatureTiming() {
        addReadyLukka(player1, 5);
        Card creature = new GrizzlyBears();
        setLibrary(creature);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    @DisplayName("-7 uses current power and the creature's lifelink")
    void minusSevenUsesCurrentPowerAndCreatureLifelink() {
        addReadyLukka(player1, 7);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.LIFELINK, 1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ColossalDreadmaw());
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 5);
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    private Permanent addReadyLukka(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LukkaCoppercoatOutcast());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
