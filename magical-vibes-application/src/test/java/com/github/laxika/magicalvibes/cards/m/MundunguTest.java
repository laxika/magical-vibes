package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FallenAskari;
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

@CardUsed({Mundungu.class, FallenAskari.class})
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
    @CardUsed(PlatinumEmperion.class)
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
}
