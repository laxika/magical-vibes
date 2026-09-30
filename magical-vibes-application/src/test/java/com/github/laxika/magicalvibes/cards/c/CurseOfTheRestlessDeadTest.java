package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
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

    private void placeCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfTheRestlessDead());
        curse.setAttachedTo(player2.getId());
    }
}
