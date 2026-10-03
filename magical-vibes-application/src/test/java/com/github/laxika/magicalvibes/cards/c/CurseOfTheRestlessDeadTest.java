package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfTheRestlessDead.class, Forest.class})
class CurseOfTheRestlessDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a decayed 2/2 Zombie when the enchanted player controls a land entering")
    void createsDecayedZombieForEnchantedPlayersLand() {
        placeCurseOnPlayer2();
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
    }

    @Test
    @DisplayName("Triggers when the Curse enchants its controller")
    void triggersWhenEnchantedPlayerIsCurseController() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfTheRestlessDead());
        curse.setAttachedTo(player1.getId());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a land controlled by another player")
    void doesNotTriggerForAnotherPlayersLand() {
        placeCurseOnPlayer2();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Can be cast enchanting an opponent and creates tokens for its controller")
    void castCurseEnchantsOpponent() {
        harness.setHand(player1, List.of(new CurseOfTheRestlessDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of the Restless Dead").getAttachedTo())
                .isEqualTo(player2.getId());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Each land entering without being played creates a separate token")
    void triggersForEachLandPutOntoBattlefield() {
        placeCurseOnPlayer2();

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("A queued trigger still creates a token after the Curse leaves")
    void queuedTriggerSurvivesCurseLeaving() {
        placeCurseOnPlayer2();
        harness.enterBattlefieldAndReturn(player2, new Forest());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("A created decayed Zombie cannot block")
    void createdZombieCannotBlock() {
        placeCurseOnPlayer2();
        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);

        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }

    @Test
    @DisplayName("An attacking decayed Zombie deals damage and is sacrificed after combat")
    void attackingZombieIsSacrificedAfterCombat() {
        placeCurseOnPlayer2();
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zombie)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, initialLife - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    private void placeCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfTheRestlessDead());
        curse.setAttachedTo(player2.getId());
    }
}
