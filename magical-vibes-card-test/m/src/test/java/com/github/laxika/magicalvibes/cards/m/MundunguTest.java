package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FallenAskari;
import com.github.laxika.magicalvibes.cards.l.LastWord;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mundungu.class, FallenAskari.class, PlatinumEmperion.class, LastWord.class})
class MundunguTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mundungu resolves to the battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new Mundungu()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gameData.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mundungu");
    }

    @Test
    @DisplayName("Counters spell when opponent cannot pay {1} and 1 life")
    void countersWhenOpponentCannotPay() {
        Permanent mundungu = addCreatureReady(player1, new Mundungu());

        harness.forceActivePlayer(player2);
        FallenAskari askari = new FallenAskari();
        harness.setHand(player2, List.of(askari));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, askari.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fallen Askari");
        harness.assertNotOnBattlefield(player2, "Fallen Askari");
        assertThat(gd.stack).isEmpty();
        assertThat(mundungu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters spell when opponent cannot pay 1 life")
    void countersWhenOpponentCannotPayLife() {
        Permanent mundungu = addCreatureReady(player1, new Mundungu());
        harness.addToBattlefield(player2, new PlatinumEmperion());

        harness.forceActivePlayer(player2);
        FallenAskari askari = new FallenAskari();
        harness.setHand(player2, List.of(askari));
        harness.addMana(player2, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, askari.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Fallen Askari");
        harness.assertNotOnBattlefield(player2, "Fallen Askari");
        assertThat(mundungu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {1} and 1 life")
    void spellNotCounteredWhenOpponentPays() {
        Permanent mundungu = addCreatureReady(player1, new Mundungu());

        harness.forceActivePlayer(player2);
        FallenAskari askari = new FallenAskari();
        harness.setHand(player2, List.of(askari));
        harness.addMana(player2, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, askari.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotInGraveyard(player2, "Fallen Askari");
        assertThat(mundungu.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Fallen Askari");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        addCreatureReady(player1, new Mundungu());

        harness.forceActivePlayer(player2);
        FallenAskari askari = new FallenAskari();
        harness.setHand(player2, List.of(askari));
        harness.addMana(player2, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, askari.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player2, "Fallen Askari");
        harness.assertNotOnBattlefield(player2, "Fallen Askari");
    }

    @Test
    @DisplayName("Can target its controller's own spell and offer payment to that controller")
    void canTargetOwnSpell() {
        addCreatureReady(player1, new Mundungu());
        FallenAskari askari = new FallenAskari();
        harness.setHand(player1, List.of(askari));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, askari.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fallen Askari");
    }

    @Test
    @DisplayName("Summoning sick Mundungu cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Mundungu());
        findPermanent(player1, "Mundungu").setSummoningSick(true);
        FallenAskari askari = new FallenAskari();
        harness.setHand(player1, List.of(askari));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, askari.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Mundungu").isTapped()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fallen Askari");
    }

    @Test
    @DisplayName("An uncounterable spell's controller may still pay mana and life")
    void canPayForUncounterableSpell() {
        addCreatureReady(player1, new Mundungu());
        FallenAskari askari = new FallenAskari();
        harness.setHand(player1, List.of(askari));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        LastWord lastWord = new LastWord();
        harness.setHand(player2, List.of(lastWord));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castInstant(player2, 0, askari.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, lastWord.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        int lifeBefore = gd.getLife(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.assertLife(player2, lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(lastWord));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fallen Askari");
        harness.assertInGraveyard(player2, "Last Word");
    }
}
