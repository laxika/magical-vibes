package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.cards.c.CavernThoctar;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skeletonize.class, DruidOfTheAnima.class, CavernThoctar.class,
        ResoundingThunder.class, BoneSplinters.class, SafePassage.class})
class SkeletonizeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage; when the targeted creature dies this turn, Skeletonize's controller creates a Skeleton token")
    void createsSkeletonWhenTargetDies() {
        harness.addToBattlefield(player2, new DruidOfTheAnima());
        harness.setHand(player1, List.of(new Skeletonize()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Druid of the Anima");
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Druid of the Anima");
        // Skeletonize's controller (player1) gets a Skeleton token
        harness.assertOnBattlefield(player1, "Skeleton");
    }

    @Test
    @DisplayName("Creates no token when the damaged creature survives the turn")
    void noTokenWhenCreatureSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        harness.setHand(player1, List.of(new Skeletonize()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Cavern Thoctar");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Skeleton");
    }

    @Test
    @DisplayName("Cannot target a player (creature-only targeting)")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Skeletonize()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsTokenWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        harness.setHand(player1, List.of(new Skeletonize(), new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player1, "Skeleton");
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Cavern Thoctar");
        harness.assertOnBattlefield(player1, "Skeleton");
        harness.assertNotOnBattlefield(player2, "Skeleton");
    }

    @Test
    void preventedDamageDoesNotQualifyCreatureForDeathTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheAnima());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima());
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);
        harness.setHand(player1, List.of(new Skeletonize(), new BoneSplinters()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Druid of the Anima");
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Druid of the Anima");
        harness.assertNotOnBattlefield(player1, "Skeleton");
    }

    @Test
    void deathOnLaterTurnDoesNotCreateToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        harness.setHand(player1, List.of(new Skeletonize()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setLibrary(player2, List.of(new DruidOfTheAnima()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DruidOfTheAnima());
        harness.setHand(player2, List.of(new BoneSplinters()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player2, 0, target.getId(), sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Cavern Thoctar");
        harness.assertNotOnBattlefield(player1, "Skeleton");
    }

    @Test
    void removedTargetDoesNotCreateToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheAnima());
        harness.setHand(player1, List.of(new Skeletonize()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Druid of the Anima");
        harness.assertInGraveyard(player1, "Skeletonize");
        harness.assertNotOnBattlefield(player1, "Skeleton");
    }

    @Test
    void skeletonTokenCanRegenerateWithBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheAnima());
        harness.setHand(player1, List.of(new Skeletonize()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Skeleton");
        UUID tokenId = token.getId();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, tokenIndex, null, null);
        resolveAllTriggers();
        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, tokenId);

        assertThat(findPermanent(player1, "Skeleton").getId()).isEqualTo(tokenId);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Skeleton");

        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, tokenId);
        harness.assertNotOnBattlefield(player1, "Skeleton");
    }
}
