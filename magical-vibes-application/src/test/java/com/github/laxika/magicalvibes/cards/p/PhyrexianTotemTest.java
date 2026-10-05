package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetherTraitor;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianTotem.class, GrizzlyBears.class, Shock.class, NetherTraitor.class, SuddenShock.class})
class PhyrexianTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Phyrexian Totem adds black mana")
    void tappingAddsBlackMana() {
        Permanent totem = addReadyTotem(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly entered noncreature Totem can produce mana")
    void noncreatureTotemDoesNotRequireHasteToProduceMana() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new PhyrexianTotem());
        totem.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Totem can animate while tapped and remains tapped")
    void tappedTotemCanAnimate() {
        Permanent totem = addReadyTotem(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(totem.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Phyrexian Totem becomes a black 5/5 Phyrexian Horror with trample")
    void animatesIntoPhyrexianHorror() {
        Permanent totem = addReadyTotem(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.isArtifact(totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, totem)).containsExactly(CardColor.BLACK);
        assertThat(totem.getTransientSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(gqs.hasKeyword(gd, totem, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isFalse();
    }

    @Test
    @DisplayName("Damage to the animated Totem makes its controller sacrifice that many permanents")
    void animatedTotemMakesControllerSacrificeThatManyPermanents() {
        Permanent totem = addReadyTotem(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        int totemIndex = gd.playerBattlefields.get(player2.getId()).indexOf(totem);
        harness.activateAbility(player2, totemIndex, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, totem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        List<Permanent> permanents = gameData.playerBattlefields.get(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, permanents.subList(1, 3).stream()
                .map(Permanent::getId)
                .toList());

        assertThat(gameData.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Phyrexian Totem");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Automatic sacrifices happen simultaneously when only two permanents remain")
    void automaticSacrificesDoNotTriggerSimultaneouslyDyingNetherTraitor() {
        harness.addToBattlefield(player2, new NetherTraitor());
        Permanent totem = addReadyTotem(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.activateAbility(player2, 1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, totem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Nether Traitor");
        harness.assertInGraveyard(player2, "Phyrexian Totem");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Combat damage makes the Totem sacrifice itself when fewer permanents remain than damage dealt")
    void combatDamageSacrificesAllAvailablePermanents() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent totem = addReadyTotem(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        totem.setBlocking(true);
        totem.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Phyrexian Totem");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lethal combat damage still requires sacrifices after the Totem dies")
    void lethalCombatDamageUsesLastKnownCreatureStatus() {
        Permanent attacker = addReadyTotem(player1);
        Permanent blocker = addReadyTotem(player2);
        harness.addToBattlefield(player2, new PhyrexianTotem());
        for (Player player : List.of(player1, player2)) {
            harness.addMana(player, ManaColor.COLORLESS, 2);
            harness.addMana(player, ManaColor.BLACK, 1);
            harness.activateAbility(player, 0, 1, null, null);
            harness.passBothPriorities();
        }

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private Permanent addReadyTotem(Player player) {
        Permanent totem = harness.addToBattlefieldAndReturn(player, new PhyrexianTotem());
        totem.setSummoningSick(false);
        return totem;
    }
}
