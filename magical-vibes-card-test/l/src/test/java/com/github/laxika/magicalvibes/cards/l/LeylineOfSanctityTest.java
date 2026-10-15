package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.b.BloodTithe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("scryfall")
@ExtendWith(CardUsedExtension.class)
@CardUsed({LeylineOfSanctity.class, BeaconOfImmortality.class, GrizzlyBears.class,
        LightningBolt.class, ProdigalPyromancer.class, BloodTithe.class})
class LeylineOfSanctityTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — leyline tests need to set hand first
    }

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Leyline of Sanctity");
        harness.assertNotInHand(player1, "Leyline of Sanctity");
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Leyline of Sanctity");
        harness.assertInHand(player1, "Leyline of Sanctity");
    }

    @Test
    @DisplayName("Opponent cannot target player with a spell when Leyline of Sanctity is on battlefield")
    void opponentCannotTargetPlayerWithSpell() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Player can still target themselves with a spell when they have Leyline of Sanctity")
    void canTargetSelfWithOwnLeyline() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("Player can still target themselves when opponent has Leyline of Sanctity")
    void canTargetSelfWhenOpponentHasLeyline() {
        harness.skipMulligan();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("Leyline of Sanctity grants hexproof to the player, not to creatures")
    void hexproofProtectsPlayerNotCreatures() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Leyline of Sanctity");
    }

    @Test
    @DisplayName("Player can be targeted after Leyline of Sanctity is removed from battlefield")
    void canTargetPlayerAfterLeylineRemoved() {
        harness.skipMulligan();
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new LeylineOfSanctity());
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(perm.getCard());

        // Now the player can be targeted
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("Leyline of Sanctity can be cast normally for {2}{W}{W}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Leyline of Sanctity");
    }

    @Test
    @DisplayName("Leyline placed from opening hand immediately grants hexproof")
    void leylineFromOpeningHandGrantsHexproof() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Leyline placement is logged")
    void leylinePlacementIsLogged() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("begins the game with Leyline of Sanctity"));
    }

    @Test
    @DisplayName("Opponents cannot target the protected player with activated abilities")
    void opponentCannotTargetPlayerWithActivatedAbility() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("The protected player can target themselves with their own activated ability")
    void controllerCanTargetSelfWithActivatedAbility() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each opening-hand Leyline can be independently accepted or declined")
    void multipleOpeningHandLeylinesHaveIndependentChoices() {
        harness.setHand(player1, List.of(new LeylineOfSanctity(), new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Leyline of Sanctity");
        harness.assertInHand(player1, "Leyline of Sanctity");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A declined Leyline in hand does not grant hexproof")
    void declinedLeylineDoesNotProtectPlayer() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Leyline of Sanctity");
    }

    @Test
    @DisplayName("Hexproof does not stop untargeted life loss from an opponent's spell")
    void hexproofDoesNotStopUntargetedLifeLoss() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new BloodTithe()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }
}
