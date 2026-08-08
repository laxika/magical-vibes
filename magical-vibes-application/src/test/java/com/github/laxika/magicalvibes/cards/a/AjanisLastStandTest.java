package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AjanisLastStandTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger sacrifices the enchantment and creates a 4/4 flying Avatar")
    void creatureDeathAcceptCreatesAvatar() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ajani's Last Stand");
        harness.assertInGraveyard(player1, "Ajani's Last Stand");
        assertAvatarToken();
    }

    @Test
    @DisplayName("Declining the death trigger keeps the enchantment and creates no token")
    void creatureDeathDeclineDoesNothing() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castWrath();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanentOrNull(player1, "Ajani's Last Stand")).isNotNull();
        assertThat(findPermanentOrNull(player1, "Avatar")).isNull();
    }

    @Test
    @DisplayName("A planeswalker you control dying also triggers the ability")
    void planeswalkerDeathTriggers() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        AjaniGoldmane ajani = new AjaniGoldmane();
        Permanent walker = new Permanent(ajani);
        walker.setCounterCount(CounterType.LOYALTY, 1);
        walker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(walker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // -1: loyalty hits 0 and Ajani dies to state-based actions.
        harness.activateAbility(player1, 1, 1, null, null);

        harness.assertInGraveyard(player1, "Ajani Goldmane");
        harness.passBothPriorities(); // resolve the loyalty ability, then the queued may-ability
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Ajani's Last Stand");
        assertAvatarToken();
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger the ability")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWrath();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanentOrNull(player1, "Avatar")).isNull();
    }

    @Test
    @DisplayName("Discarded by an opponent while controlling a Plains creates the Avatar")
    void discardedByOpponentWithPlainsCreatesAvatar() {
        harness.addToBattlefield(player2, new Plains());
        discardToPlayer2();

        assertThat(findPermanentOrNull(player2, "Avatar")).isNotNull();
    }

    @Test
    @DisplayName("Discarded by an opponent without a Plains creates nothing")
    void discardedByOpponentWithoutPlainsDoesNothing() {
        discardToPlayer2();

        assertThat(findPermanentOrNull(player2, "Avatar")).isNull();
    }

    private void discardToPlayer2() {
        harness.setHand(player2, new ArrayList<>(List.of(new AjanisLastStand(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard Ajani's Last Stand
        harness.handleCardChosen(player2, 0); // discard Grizzly Bears
        harness.passBothPriorities(); // resolve the discard trigger
    }

    private void castWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath - creatures die
        harness.passBothPriorities(); // resolve the death may-effect prompt (if any)
    }

    private void assertAvatarToken() {
        Permanent token = findPermanentOrNull(player1, "Avatar");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
