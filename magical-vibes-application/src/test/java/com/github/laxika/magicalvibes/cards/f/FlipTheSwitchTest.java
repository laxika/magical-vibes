package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlipTheSwitch.class, Shock.class})
class FlipTheSwitchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and creates a decayed Zombie when its controller cannot pay {4}")
    void countersSpellAndCreatesZombieWhenControllerCannotPay() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FlipTheSwitch()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertDecayedZombieCreated();
    }

    @Test
    @DisplayName("Lets the target spell resolve when its controller pays {4} and still creates a Zombie")
    void letsSpellResolveWhenControllerPays() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setHand(player1, List.of(new FlipTheSwitch()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 18);
        assertDecayedZombieCreated();
    }

    @Test
    @DisplayName("Counters the spell and creates a Zombie when its controller declines payment")
    void decliningPaymentStillCreatesZombie() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setHand(player1, List.of(new FlipTheSwitch()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertDecayedZombieCreated();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not create a Zombie when its only target has left the stack")
    void missingTargetPreventsTokenCreation() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FlipTheSwitch(), new FlipTheSwitch()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.castInstant(player1, 0, shock.getId());

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created Zombie deals combat damage and is sacrificed at end of combat")
    void attackingZombieIsSacrificedAtEndOfCombat() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FlipTheSwitch()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zombie)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    private void assertDecayedZombieCreated() {
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }
}
