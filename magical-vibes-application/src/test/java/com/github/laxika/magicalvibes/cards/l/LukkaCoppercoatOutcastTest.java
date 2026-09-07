package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent otherLukka = new Permanent(new LukkaBoundToRuin());
        otherLukka.setCounterCount(CounterType.LOYALTY, 5);
        otherLukka.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherLukka);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
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

    private Permanent addReadyLukka(Player player, int loyalty) {
        Permanent permanent = new Permanent(new LukkaCoppercoatOutcast());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
